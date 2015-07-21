/*
 * Copyright (C) 2015 PÂRIS Quentin
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.playonlinux.core.scripts;

import com.playonlinux.framework.ScriptFailureException;
import com.playonlinux.core.python.PythonInstaller;
import org.apache.commons.lang.StringUtils;
import org.python.util.PythonInterpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.concurrent.ExecutorService;

public class ScriptRecent extends Script {
    protected ScriptRecent(String script, ExecutorService executorService) {
        super(script, executorService);
    }

    @Override
    protected void executeScript(PythonInterpreter pythonInterpreter) throws ScriptFailureException {
        pythonInterpreter.exec(this.getScriptContent());
        PythonInstaller<ScriptTemplate> pythonInstaller = new PythonInstaller<>(pythonInterpreter, ScriptTemplate.class);

        pythonInstaller.exec();
    }



    @Override
    public String extractSignature() throws ParseException, IOException {
        BufferedReader bufferReader = new BufferedReader(new StringReader(this.getScriptContent()));
        StringBuilder signatureBuilder = new StringBuilder();

        Boolean insideSignature = false;
        do {
            String readLine = bufferReader.readLine();
            if(readLine == null) {
                break;
            }
            if(readLine.contains("-----BEGIN PGP PUBLIC KEY BLOCK-----") && readLine.startsWith("#")) {
                insideSignature = true;
            }

            if(insideSignature) {
                if(readLine.startsWith("#")) {
                    signatureBuilder.append(readLine.substring(1, readLine.length()).trim());
                }
                signatureBuilder.append("\n");
            }

            if(readLine.contains("-----END PGP PUBLIC KEY BLOCK-----") && readLine.startsWith("#")) {
                insideSignature = false;
            }
        } while(true);

        String signature = signatureBuilder.toString().trim();

        if(StringUtils.isBlank(signature)) {
            throw new ParseException("The script has no valid signature!", 0);
        }
        return signature;
    }
}