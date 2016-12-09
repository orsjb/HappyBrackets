/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.core;

/**
 * Generic interface for processes that wish to receive notification of errors occuring in other components.
 * Used to communicate errors to the plugin if available.
 */
public interface ErrorListener {
    /**
     * @param clazz The class the error occurred in.
     * @param description The description of the error. May be null.
     * @param ex The exception that was thrown if applicable, otherwise null.
     */
    public void errorOccurred(Class clazz, String description, Exception ex);
}
