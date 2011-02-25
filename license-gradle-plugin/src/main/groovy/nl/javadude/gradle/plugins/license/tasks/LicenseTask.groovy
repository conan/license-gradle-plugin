/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package nl.javadude.gradle.plugins.license.tasks

import org.gradle.api.GradleException

import org.gradle.api.tasks.TaskAction
class LicenseTask extends AbstractLicenseTask {
    def licenseCache = [:]
    List<String> licenseLines

    List<java.lang.String> loadLicense() {
        def license = project.convention.plugins.license.license
        if (!license.exists()) {
            throw new GradleException("The license file [" + license + "] does not exist.")
        }
        license.readLines()
    }

    @TaskAction
    protected void process() {
        licenseLines = loadLicense()
        toBeLicensed = scanForFiles()
        toBeLicensed.findAll({ shouldBeLicensed it }).each { file ->
            licenseFile(file)
        }
    }

    def getLicenseForFile(File file) {
        def ext = file.name.substring(file.name.indexOf('.') + 1)
        if (!licenseCache[ext]) {
            format = project.licenseTypes[ext]
            licenseCache[ext] = format.transform(licenseLines)
        }
        licenseCache[ext]
    }

    boolean shouldBeLicensed(File file) {
        def license = getLicenseForFile(file)
        !license.isLicensed(file)
    }

    def licenseFile(File file) {
        println "Adding license on " + file
        def license = getLicenseForFile(file)
        def lines = file.readLines()
        file.delete()
        file.createNewFile()
        file.withWriter { w ->
            license.lines.each { line ->
                w.writeLine(line)
            }
            lines.each { line ->
                w.writeLine(line)
            }
            w.newLine()
        }
    }
}
