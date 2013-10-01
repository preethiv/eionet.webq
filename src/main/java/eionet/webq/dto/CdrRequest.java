/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Web Questionnaires 2
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Anton Dmitrijev
 */
package eionet.webq.dto;

/**
 */
public class CdrRequest {
    /**
     * Envelope URL.
     */
    private String envelopeUrl;
    /**
     * Only requested schema.
     */
    private String schema;
    /**
     * Allow to create new form.
     */
    private boolean newFormCreationAllowed;
    /**
     * New file name.
     */
    private String newFileName;
    /**
     * Is authorization set.
     */
    private boolean authorizationSet;
    /**
     * Authentication user name.
     */
    private String userName;
    /**
     * Authentication password.
     */
    private String password;
    /**
     * URL of xml file stored in CDR.
     */
    private String instanceUrl;
    /**
     * XML file title.
     */
    private String instanceTitle;
    /**
     * Additional parameters passed with this request.
     */
    private String additionalParametersAsQueryString;
    /**
     * Request context path.
     */
    private String contextPath;

    public String getEnvelopeUrl() {
        return envelopeUrl;
    }

    public void setEnvelopeUrl(String envelopeUrl) {
        this.envelopeUrl = envelopeUrl;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public boolean isNewFormCreationAllowed() {
        return newFormCreationAllowed;
    }

    public void setNewFormCreationAllowed(boolean newFormCreationAllowed) {
        this.newFormCreationAllowed = newFormCreationAllowed;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    public boolean isAuthorizationSet() {
        return authorizationSet;
    }

    public void setAuthorizationSet(boolean authorizationSet) {
        this.authorizationSet = authorizationSet;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getInstanceUrl() {
        return instanceUrl;
    }

    public void setInstanceUrl(String instanceUrl) {
        this.instanceUrl = instanceUrl;
    }

    public String getInstanceTitle() {
        return instanceTitle;
    }

    public void setInstanceTitle(String instanceTitle) {
        this.instanceTitle = instanceTitle;
    }

    public String getAdditionalParametersAsQueryString() {
        return additionalParametersAsQueryString;
    }

    public void setAdditionalParametersAsQueryString(String additionalParametersAsQueryString) {
        this.additionalParametersAsQueryString = additionalParametersAsQueryString;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
}