/*
 * Copyright (c) 2016-2026 Jumin Rubin
 * LinkedIn: https://www.linkedin.com/in/juminrubin/
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
package org.jrtech.common.preferences;

import java.util.List;

public interface PreferenceService {

    // General

    public Preference retrievePreference(String id);

    // Organization

    public Preference createOrganizationPreference(String organizationUnitId, Preference preference);

    public Preference modifyOrganizationPreference(String organizationUnitId, Preference preference);

    public void destroyOrganizationPreference(String organizationUnitId, String preferenceId);

    public List<Preference> retrieveOrganizationPreferenceList(String organizationUnitId);

    public List<Preference> retrieveOrganizationPreferenceListByType(String organizationUnitId, String type);

    public List<Preference> retrieveOrganizationPreferenceListByTypeModule(String organizationUnitId, String type,
            String module);

    public List<Preference> retrieveOrganizationPreferenceListByTypeModuleEntity(String organizationUnitId, String type,
            String module, String entity);

    public Preference retrieveOrganizationPreference(String organizationUnitId, String type, String module,
            String entity, String name);

    // User

    public Preference createUserPreference(Preference preference);

    public Preference modifyUserPreference(Preference preference);

    public void destroyUserPreference(String preferenceId);

    public List<Preference> retrieveUserPreferenceListByUser(String userId);

    public List<Preference> retrieveUserPreferenceListByType(String type);

    public List<Preference> retrieveUserPreferenceListByTypeModule(String type, String module);

    public List<Preference> retrieveUserPreferenceListByTypeModuleEntity(String type, String module, String entity);

    public Preference retrieveUserPreference(String type, String module, String entity, String name);
}
