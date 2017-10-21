package org.apache.archiva.repository;

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

import org.apache.archiva.configuration.ManagedRepositoryConfiguration;
import org.apache.archiva.configuration.RemoteRepositoryConfiguration;

import java.util.Set;

/**
 *
 * This interface must be implemented by the repository implementations. These
 * are responsible for creating the instances.
 *
 */
public interface RepositoryProvider
{
    Set<RepositoryType> provides();

    ManagedRepository createManagedInstance( ManagedRepositoryConfiguration configuration) throws RepositoryException;

    ManagedRepository createStagingInstance(ManagedRepositoryConfiguration baseConfiguration) throws RepositoryException;

    RemoteRepository createRemoteInstance( RemoteRepositoryConfiguration configuration) throws RepositoryException;

    RemoteRepositoryConfiguration getRemoteConfiguration(RemoteRepository remoteRepository) throws RepositoryException;

    ManagedRepositoryConfiguration getManagedConfiguration(ManagedRepository managedRepository) throws RepositoryException;
}