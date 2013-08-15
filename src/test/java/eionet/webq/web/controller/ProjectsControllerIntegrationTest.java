package eionet.webq.web.controller;

import eionet.webq.dao.ProjectFolders;
import eionet.webq.dto.ProjectEntry;
import eionet.webq.dto.WebFormUpload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collection;

import static eionet.webq.web.controller.ProjectsController.PROJECT_ENTRY_MODEL_ATTRIBUTE;
import static eionet.webq.web.controller.ProjectsController.WEB_FORM_UPLOAD_ATTRIBUTE;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
@RunWith(SpringJUnit4ClassRunner.class)
public class ProjectsControllerIntegrationTest extends AbstractProjectsControllerTests {
    private static final String ADD_EDIT_PROJECT_VIEW = "add_edit_project";
    private static final String DEFAULT_PROJECTID = "DEFAULT_PROJECTID";
    public static final String PROJECT_ID_PARAM = "projectId";
    @Autowired
    private ProjectFolders projectFolders;

    @Test
    public void returnsAllProjectsViewName() throws Exception {
        request(get("/projects/")).andExpect(view().name("projects"));
    }

    @Test
    public void modelCollectionIsEmptyIfNoProjects() throws Exception {
        Collection<ProjectEntry> allProjects = getAllProjectEntries();
        assertThat(allProjects.size(), equalTo(0));
    }

    @Test
    public void allProjectsStoredInDataStorageArePresentInModel() throws Exception {
        saveProjectWithId("1");
        saveProjectWithId("2");

        assertThat(getAllProjectEntries().size(), equalTo(2));
    }

    @Test
    public void allowToAddNewProject() throws Exception {
        addNewProject("1", "short description");
        assertThat(getAllProjectEntries().size(), equalTo(1));
    }

    @Test
    public void allowToRemoveProject() throws Exception {
        String projectId = "projectToRemove";
        addNewProject(projectId, "project");
        assertThat(getAllProjectEntries().size(), equalTo(1));

        request(get("/projects/remove?projectId=" + projectId));
        assertThat(getAllProjectEntries().size(), equalTo(0));
    }

    @Test
    public void emptyObjectIsLoadedToModelWhenCreatingNewProject() throws Exception {
        ResultActions actions = request(get("/projects/add"));
        ProjectEntry projectEntry = assertViewNameAndReturnProjectEntryFromModel(actions, ADD_EDIT_PROJECT_VIEW);

        assertThat(projectEntry.getId(), equalTo(0));
        assertNull(projectEntry.getProjectId());
        assertNull(projectEntry.getDescription());
        assertNull(projectEntry.getCreated());
    }

    @Test
    public void loadsRequiredProjectForEdit() throws Exception {
        String projectToEdit = "projectToEdit";
        saveProjectWithId(projectToEdit);
        ResultActions actions = request(get("/projects/edit").param(PROJECT_ID_PARAM, projectToEdit));

        ProjectEntry projectEntry = assertViewNameAndReturnProjectEntryFromModel(actions, ADD_EDIT_PROJECT_VIEW);

        assertThat(projectEntry.getProjectId(), equalTo(projectToEdit));
        assertNotNull(projectEntry.getId());
        assertNotNull(projectEntry.getCreated());
    }

    @Test
    public void allowToEditProject() throws Exception {
        String projectId = "projectToEdit";
        saveProjectWithId(projectId);
        ProjectEntry byProjectId = projectFolders.getByProjectId(projectId);

        String projectIdUpdated = "projectIdUpdated";
        String newDescription = "newDescription";
        MockHttpServletRequestBuilder post =
                post("/projects/save").param("id", Integer.toString(byProjectId.getId())).param(PROJECT_ID_PARAM, projectIdUpdated)
                        .param("description", newDescription);
        request(post);

        ProjectEntry updatedProject = projectFolders.getByProjectId(projectIdUpdated);

        assertThat(updatedProject.getId(), equalTo(byProjectId.getId()));
        assertThat(updatedProject.getProjectId(), equalTo(projectIdUpdated));
        assertThat(updatedProject.getDescription(), equalTo(newDescription));
    }

    @Test
    public void allowToViewProjectFolderContent() throws Exception {
        saveProjectWithId(DEFAULT_PROJECTID);
        ResultActions request = request(get("/projects/" + DEFAULT_PROJECTID + "/view"));
        ProjectEntry project = assertViewNameAndReturnProjectEntryFromModel(request, "view_project");

        assertThat(project.getProjectId(), equalTo(DEFAULT_PROJECTID));
    }

    @Test
    public void allowToUploadAWebFormForAProject() throws Exception {
        saveProjectWithId(DEFAULT_PROJECTID);
        String title = "test webform";
        byte[] content = "my-webform".getBytes();
        ResultActions request = request(fileUpload("/projects/" + DEFAULT_PROJECTID + "/webform/new").file("file", content)
                .param("title", title).param("status", "true"));

        WebFormUpload webformUpload = (WebFormUpload) request.andReturn().getModelAndView().getModel().get(WEB_FORM_UPLOAD_ATTRIBUTE);

        assertThat(webformUpload.getTitle(), equalTo(title));
        assertThat(webformUpload.getFile(), equalTo(content));
    }

    private void saveProjectWithId(String projectId) {
        projectFolders.save(projectEntryWith(projectId));
    }

    private ProjectEntry assertViewNameAndReturnProjectEntryFromModel(ResultActions actions, String viewName) throws Exception {
        MvcResult mvcResult = actions.andExpect(view().name(viewName)).andReturn();
        return (ProjectEntry) mvcResult.getModelAndView().getModel().get(PROJECT_ENTRY_MODEL_ATTRIBUTE);
    }

    private ProjectEntry projectEntryWith(String id) {
        ProjectEntry projectEntry = new ProjectEntry();
        projectEntry.setProjectId(id);
        return projectEntry;
    }

    @SuppressWarnings("unchecked")
    private Collection<ProjectEntry> getAllProjectEntries() throws Exception {
        MvcResult mvcResult = request(get("/projects/")).andReturn();
        return (Collection<ProjectEntry>) mvcResult.getModelAndView().getModelMap().get("allProjects");
    }
}
