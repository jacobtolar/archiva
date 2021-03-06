package org.apache.archiva.repository.features;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.archiva.repository.Repository;
import org.apache.archiva.repository.RepositoryEvent;

import java.net.URI;

public class IndexCreationEvent extends RepositoryEvent<URI> {

    public enum Index implements EventType {
        INDEX_URI_CHANGE, PACKED_INDEX_URI_CHANGE
    }

    IndexCreationEvent(Repository repo, URI oldValue, URI value) {
        super(Index.INDEX_URI_CHANGE, repo, oldValue, value);
    }

    IndexCreationEvent(Index type, Repository repo, URI oldValue, URI value) {
        super(type, repo, oldValue, value);
    }

    public static final IndexCreationEvent indexUriChange(Repository repo, URI oldValue, URI newValue) {
        return new IndexCreationEvent(Index.INDEX_URI_CHANGE, repo, oldValue, newValue);
    }

    public static final IndexCreationEvent packedIndexUriChange(Repository repo, URI oldValue, URI newValue) {
        return new IndexCreationEvent(Index.PACKED_INDEX_URI_CHANGE, repo, oldValue, newValue);
    }
}
