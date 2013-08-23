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
package eionet.webq.web.controller;

import eionet.webq.dao.FileStorage;
import eionet.webq.dto.ProjectEntry;
import eionet.webq.dto.ProjectFile;
import eionet.webq.dto.UserFile;
import eionet.webq.service.ConversionService;
import eionet.webq.service.ProjectService;
import eionet.webq.service.UserFileService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Spring controller for WebQ file download.
 */
@Controller
@RequestMapping("download")
public class FileDownloadController {
    /**
     * Service for getting user file content from storage.
     */
    @Autowired
    private UserFileService userFileService;
    /**
     * Service for getting user file content from storage.
     */
    @Autowired
    private ProjectService projectService;
    /**
     * Service for getting project file content from storage.
     */
    @Autowired
    @Qualifier("project-files")
    private FileStorage<ProjectEntry, ProjectFile> projectFiles;
    /**
     * File conversion service.
     */
    @Autowired
    private ConversionService conversionService;

    /**
     * Download uploaded file action.
     *
     * @param fileId requested file id
     * @param response http response to write file
     */
    @RequestMapping(value = "/user_file")
    public void downloadUserFile(@RequestParam int fileId, HttpServletResponse response) {
        UserFile file = userFileService.getById(fileId);
        addXmlFileHeaders(response, file.getName());
        writeToResponse(response, file.getContent());
    }

    /**
     * Download uploaded file action.
     *
     * @param projectId project id for file download
     * @param fileId requested file id
     * @param response http response to write file
     */
    @RequestMapping(value = "/project/{projectId}/file/{fileId}")
    public void downloadProjectFile(@PathVariable String projectId, @PathVariable int fileId, HttpServletResponse response) {
        ProjectFile projectFile = projectFiles.fileContentBy(fileId, projectService.getByProjectId(projectId));
        addXmlFileHeaders(response, encodeAsUrl(projectFile.getFileName()));
        writeToResponse(response, projectFile.getFileContent());
    }

    /**
     * Performs conversion of specified {@link eionet.webq.dto.UserFile} to specific format.
     * Format is defined by conversionId.
     * @param fileId file id, which will be loaded and converted
     * @param conversionId id of conversion to be used
     * @param response object where conversion result will be written
     */
    @RequestMapping("/convert")
    public void convertXmlFile(@RequestParam int fileId, @RequestParam int conversionId, HttpServletResponse response) {
        UserFile fileContent = userFileService.getById(fileId);
        ResponseEntity<byte[]> convert = conversionService.convert(fileContent, conversionId);
        HttpHeaders headers = convert.getHeaders();
        setContentType(response, headers.getContentType());
        setContentDisposition(response, headers.getFirst("Content-Disposition"));
        writeToResponse(response, convert.getBody());
    }

    /**
     * Sets headers required to xml file download.
     *
     * @param response http response
     * @param fileName file name
     */
    private void addXmlFileHeaders(HttpServletResponse response, String fileName) {
        setContentType(response, MediaType.APPLICATION_XML);
        setContentDisposition(response, "attachment;filename=" + fileName);
    }

    /**
     * Sets content disposition to response.
     *
     * @param response {@link HttpServletResponse}
     * @param contentDisposition content disposition header value.
     */
    private void setContentDisposition(HttpServletResponse response, String contentDisposition) {
        if (contentDisposition != null) {
            response.setHeader("Content-Disposition", contentDisposition);
        }
    }

    /**
     * Sets content type header to response.
     *
     * @param response http response
     * @param contentType content type
     */
    private void setContentType(HttpServletResponse response, MediaType contentType) {
        if (contentType != null) {
            response.setContentType(contentType.toString());
        }
    }

    /**
     * {@link URLEncoder#encode(String, String)} with default value.
     *
     * @param path string to encode, also default value
     * @return encoded string or default value.
     */
    private String encodeAsUrl(String path) {
        try {
            return URLEncoder.encode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return path;
        }
    }

    /**
     * Writes specified content to http response.
     * @param response http response
     * @param data content to be written to response
     */
    private void writeToResponse(HttpServletResponse response, byte[] data) {
        ServletOutputStream output = null;
        try {
            response.setContentLength(data.length);

            output = response.getOutputStream();
            IOUtils.write(data, output);
            output.flush();
        } catch (IOException e) {
            throw new RuntimeException("Unable to write response", e);
        } finally {
            IOUtils.closeQuietly(output);
        }
    }
}