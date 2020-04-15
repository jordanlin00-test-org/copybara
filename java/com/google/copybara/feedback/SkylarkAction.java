/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.copybara.feedback;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.copybara.SkylarkContext;
import com.google.copybara.exception.RepoException;
import com.google.copybara.exception.ValidationException;
import com.google.devtools.build.lib.syntax.Dict;
import com.google.devtools.build.lib.syntax.EvalException;
import com.google.devtools.build.lib.syntax.Starlark;
import com.google.devtools.build.lib.syntax.StarlarkCallable;
import com.google.devtools.build.lib.syntax.StarlarkThread;
import java.util.function.Supplier;

/**
 * An implementation of {@link Action} that delegates to a Skylark function.
 */
public class SkylarkAction implements Action {

  private final StarlarkCallable function;
  private final Dict<?, ?> params;
  private final Supplier<StarlarkThread> thread;

  public SkylarkAction(
      StarlarkCallable function, Dict<?, ?> params, Supplier<StarlarkThread> thread) {
    this.function = Preconditions.checkNotNull(function);
    this.params = Preconditions.checkNotNull(params);
    this.thread = Preconditions.checkNotNull(thread);
  }

  @Override
  public void run(SkylarkContext<?> context) throws ValidationException, RepoException {
    try {
      //noinspection unchecked
      SkylarkContext<?> actionContext = (SkylarkContext<?>) context.withParams(params);
      Object result =
          Starlark.call(
              thread.get(),
              function,
              ImmutableList.of(actionContext),
              /*kwargs=*/ ImmutableMap.of());
      context.onFinish(result, actionContext);
    } catch (IllegalArgumentException e) {
      throw new ValidationException("Error calling Skylark:", e);
    } catch (EvalException e) {
      Throwable cause = e.getCause();
      String error =
          String.format("Error while executing the skylark transformation %s: %s. Location: %s",
              function.getName(), e.getMessage(), e.getLocation());
      if (cause instanceof RepoException) {
        throw new RepoException(error, cause);
      }
      throw new ValidationException(error, cause);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("This should not happen.", e);
    }
  }

  @Override
  public String getName() {
    return function.getName();
  }

  @Override
  public ImmutableSetMultimap<String, String> describe() {
    ImmutableSetMultimap.Builder<String, String> builder = ImmutableSetMultimap.builder();
    for (Object paramKey : params.keySet()) {
      builder.put(paramKey.toString(), params.get(paramKey).toString());
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", function.getName())
        .toString();

  }
}
