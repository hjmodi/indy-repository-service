/**
 * Copyright (C) 2011-2022 Red Hat, Inc. (https://github.com/Commonjava/indy-repository-service)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.indy.service.repository.data.metrics;

public class TraceConstant
{
    public static final String DEFAULT = "default";

    public static final String EXCEPTION = "exception";

    public static final String SKIP_METRIC = "skip-this-metric";

    public static final String CUMULATIVE_TIMINGS = "cumulative-timings";

    public static final String CUMULATIVE_COUNT = "cumulative-count";

    public static final String AVERAGE_TIME_MS = "avg-time-ms";

    public static final String MAX_TIME_MS = "max-time-ms";

    public static final double NANOS_PER_MILLISECOND = 1E6;
}
