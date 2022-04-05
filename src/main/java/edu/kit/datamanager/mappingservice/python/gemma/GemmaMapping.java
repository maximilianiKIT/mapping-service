/*
 * Copyright 2019 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.mappingservice.python.gemma;

import edu.kit.datamanager.mappingservice.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.indexer.mapping.IMappingTool;
import edu.kit.datamanager.mappingservice.indexer.mapping.MappingUtil;
import edu.kit.datamanager.mappingservice.indexer.util.IndexerUtil;
import edu.kit.datamanager.mappingservice.python.util.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.validator.util.ValidatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static edu.kit.datamanager.mappingservice.indexer.util.IndexerUtil.DEFAULT_SUFFIX;

/**
 * Utilities class for GEMMA.
 */
public class GemmaMapping implements IMappingTool {

  /** Logger for this class.
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(GemmaMapping.class);

  
  GemmaConfiguration gemmaConfiguration;

  public GemmaMapping(ApplicationProperties configuration) throws URISyntaxException, MalformedURLException {
    gemmaConfiguration = new GemmaConfiguration();
    File gemmaFile = new File(configuration.getGemmaLocation().getPath());
    File pythonExecutable = new File(configuration.getPythonLocation().getPath());
    gemmaConfiguration.setGemmaLocation(gemmaFile.toURI().toURL());
    gemmaConfiguration.setPythonLocation(pythonExecutable.toURI().toURL());
  }

  /**
   * Map the source file to a new file using a given mapping tool.
   *
   * @param mappingFile The absolute path to mapping file.
   * @param srcFile The absolute path to the source file.
   * @param resultFile The absolute path to the created mapping.
    *
   * @return Errorcode (0 = SUCCESS)
   * @see edu.kit.datamanager.mappingservice.python.util.PythonUtils
   */
  public int mapFile(Path mappingFile, Path srcFile, Path resultFile){
    LOGGER.trace("Run gemma on '{}' with mapping '{}' -> '{}'", srcFile, mappingFile, resultFile);
     int returnCode = PythonUtils.run(gemmaConfiguration.getPythonLocation().getPath(), gemmaConfiguration.getGemmaLocation().getPath(), mappingFile.toAbsolutePath().toString(), srcFile.toAbsolutePath().toString(), resultFile.toAbsolutePath().toString());
   return returnCode;
  }

  public int mapFile(Path mappingFile, InputStream src, OutputStream result){
    Path gemmaInput = IndexerUtil.createTempFile("gemmaInput", "");
    Path gemmaOutput = IndexerUtil.createTempFile("gemmaOutput", "");
    try {
      Files.copy(src, gemmaInput, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      e.printStackTrace();
    }

    LOGGER.trace("Run gemma on '{}' with mapping '{}' -> '{}'", gemmaInput, mappingFile, gemmaOutput);
    int returnCode = PythonUtils.run(gemmaConfiguration.getPythonLocation().getPath(), gemmaConfiguration.getGemmaLocation().getPath(), mappingFile.toAbsolutePath().toString(), gemmaInput.toAbsolutePath().toString(), gemmaOutput.toAbsolutePath().toString());

    File outFile = gemmaOutput.toFile();
    try {
      result = new FileOutputStream(outFile);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    new File(gemmaInput.toUri()).delete();
    new File(gemmaOutput.toUri()).delete();

    return returnCode;
  }
}
