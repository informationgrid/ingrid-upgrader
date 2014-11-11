/*
 * **************************************************-
 * ingrid-upgrader
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.upgrader.model;

public interface IKeys {

    public static final String ID_FIELD = "feed_id";

    public static final String PATH_FIELD = "path";

    public static final String UPDATED_FIELD = "updated";

    public static final String IPLUG_TYPE_FIELD = "ingridComponentType";

    public static final String VERSION_FIELD = "Implementation-Version";
    
    public static final String BUILD_FIELD = "Implementation-Build";

    public static final String INDEX_FOLDER = "index";

    public static final String TEMP_FOLDER = "tmpIndex";

    public static final String INDEX_IDENTIFIER = "index";

    public static final String SOURCE_IDENTIFIER = "source";

    public static final String PERIOD_IDENTIFIER = "period";

    public static final String ID_PARAMETER = "id";

    public static final String CONTEXT_IDENTIFIER = "context";
    
    public static final String CHANGELOG_FILE = "/site/changes-report.html";
    
    public static final String CHANGELOG_STYLE = "/site/css/maven-theme.css";
}
