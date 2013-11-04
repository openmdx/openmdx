/*
 *
 * EPP - the easy preprocessor ant task
 *
 * Copyright 2009 Dmitriy Rykov
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
 *
 */
package rykov.epp;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.MatchResult;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class EppTask extends Task {

    private static final String IFDEF   = "ifdef";

    private static final String IFNDEF  = "ifndef";

    private static final String ELSE   = "else";

    private static final String ENDIF   = "endif";

    private static final String INCLUDE = "include";

    private String srcfile;

    private String destfile;

    private String defs;

    private String incdir;

    public void setSrcfile(String srcfile) {
        this.srcfile = srcfile;
    }

    public void setDestfile(String destfile) {
        this.destfile = destfile;
    }

    public void setDefs(String defs) {
        this.defs = defs;
    }

    public void setIncdir(String incdir) {
        this.incdir = incdir;
    }

    public void execute() throws BuildException {
        if (srcfile == null) {
            throw new BuildException("Input file parameter missed");
        }
        if (destfile == null) {
            throw new BuildException("Output file parameter missed");
        }
        if (defs == null) {
            throw new BuildException("Definition list parameter missed");
        }
        try {
            preprocess();
        } catch (IOException e) {
            throw new BuildException("IO error while preprocessing", e);
        }
    }
    private void preprocess() throws IOException {
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new FileReader(srcfile));
            bw = new BufferedWriter(new FileWriter(destfile));
            Set set = new HashSet(Arrays.asList(defs.replaceAll("\\s+", "").split(",")));
            Pattern pattern = Pattern.compile("^\\s*#(" + IFDEF + "|" + IFNDEF + "|" +
                    ELSE + "|" + ENDIF + "|" + INCLUDE  + ")\\s*(\\S*)\\s*$");
            String line;
            int currentLevel = 0;
            int removeLevel = -1;
            boolean skip = false;
            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    MatchResult result = matcher.toMatchResult();
                    String dir = result.group(1);
                    String param = result.group(2);
                    if (IFDEF.equals(dir)) {
                        skip = !set.contains(param);
                        if (removeLevel == -1 && skip) {
                            removeLevel = currentLevel;
                        }
                        currentLevel++;
                        continue;
                    } else if (IFNDEF.equals(dir)) {
                        skip = set.contains(param);
                        if (removeLevel == -1 && skip) {
                            removeLevel = currentLevel;
                        }
                        currentLevel++;
                        continue;
                    } else if (ELSE.equals(dir)) {
                        currentLevel--;
                        if (currentLevel == removeLevel) {
                            removeLevel = -1;
                        }
                        if (removeLevel == -1 && !skip) {
                            removeLevel = currentLevel;
                        }
                        currentLevel++;
                        continue;
                    } else if (ENDIF.equals(dir)) {
                        currentLevel--;
                        if (currentLevel == removeLevel) {
                            removeLevel = -1;
                        }
                        continue;
                    } else if (INCLUDE.equals(dir)) {
                        include(param, bw);
                        continue;
                    }
                }
                if (removeLevel == -1 || currentLevel < removeLevel) {
                    bw.write(line);
                    bw.newLine();
                }
            }
        } finally {
            if (br != null) {
                br.close();
            }
            if (bw != null) {
                bw.close();
            }
        }
    }
    private void include(String file, BufferedWriter bw) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(incdir, file)));
            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine();
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }
}
