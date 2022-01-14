/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Macro Provider 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.dataprovider.layer.persistence.jdbc.macros;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.w3c.cci2.SparseArray;

/**
 * Macro Provider
 */
public class ClassicMacroConfiguration implements MacroConfiguration {

    public ClassicMacroConfiguration(
        final Supplier<SparseArray<String>> stringMacroColumns,
        final Supplier<SparseArray<String>> stringMacroNames,
        final Supplier<SparseArray<String>> stringMacroValues,
        final Supplier<SparseArray<String>> pathMacroNames,
        final Supplier<SparseArray<String>> pathMacroValues
    ) {
        this.stringMacroColumns = stringMacroColumns;
        this.stringMacroNames = stringMacroNames;
        this.stringMacroValues = stringMacroValues;
        this.pathMacroNames = pathMacroNames;
        this.pathMacroValues = pathMacroValues;
    }

    /**
     * Lazy evaluation of the suppliers get() method to retrieve <ode>stringMacroColumn</code>
     */
    private final Supplier<SparseArray<String>> stringMacroColumns;

    /**
     * Lazy evaluation of the suppliers get() method to retrieve <ode>stringMacroName</code>
     */
    private final Supplier<SparseArray<String>> stringMacroNames;

    /**
     * Lazy evaluation of the suppliers get() method to retrieve <ode>stringMacroValue</code>
     */
    private final Supplier<SparseArray<String>> stringMacroValues;

    /**
     * Lazy evaluation of the suppliers get() method to retrieve <ode>pathMacroName</code>
     */
    private final Supplier<SparseArray<String>> pathMacroNames;

    /**
     * Lazy evaluation of the suppliers get() method to retrieve <ode>pathMacroValue</code>
     */
    private final Supplier<SparseArray<String>> pathMacroValues;

    /**
     * The macro handler is lazily instantiated
     */
    private MacroHolder macroHolder;

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.macros.MacroConfiguration#getMacroHandler()
     */
    @Override
    public MacroHandler getMacroHandler() {
        return getMacroHolder();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.macros.MacroConfiguration#getMacros()
     */
    @Override
    public Map<String, List<StringMacro>> getStringMacros() {
        return getMacroHolder().getStringMacros();
    }

    private MacroHolder getMacroHolder() {
        if (macroHolder == null) {
            macroHolder = new MacroHolder(
                determineStringMacros(
                    stringMacroColumns.get(),
                    stringMacroNames.get(),
                    stringMacroValues.get()
                ),
                determinePathMacros(
                    pathMacroNames.get(),
                    pathMacroValues.get()
                )
            );
        }
        return macroHolder;
    }

    /**
     * Create a macro configuration based on the given Java Bean configuration
     * 
     * @param macroColumns
     *            the <code>stringMacroColumn</code> configuration
     * @param macroNames
     *            the <code>stringMacroNames</code> configuration
     * @param macroValues
     *            the <code>stringMacroValue</code> configuration
     * 
     * @return a new macro handler
     */
    private Map<String, List<StringMacro>> determineStringMacros(
        final SparseArray<String> macroColumns,
        final SparseArray<String> macroNames,
        final SparseArray<String> macroValues
    ) {
        final Map<String, List<StringMacro>> macros = new HashMap<>();
        for (final ListIterator<String> columnIterator = macroColumns.populationIterator(); columnIterator.hasNext();) {
            final Integer i = Integer.valueOf(columnIterator.nextIndex());
            final String columnName = columnIterator.next();
            final String macroName = Objects.requireNonNull(
                macroNames.get(i),
                () -> "Missing string macro name with index " + i + " \uC2AB" + columnName + "\uC2BB"
            );
            final String macroValue = Objects.requireNonNull(
                macroValues.get(i),
                () -> "Missing string macro value with index " + i + " for column \uC2AB" + columnName + "\uC2BB"
            );
            List<StringMacro> entries = macros.get(columnName);
            if (entries == null) {
                macros.put(
                    columnName,
                    entries = new ArrayList<>()
                );
            }
            entries.add(
                new StringMacro(
                    macroName,
                    macroValue
                )
            );
        }
        return macros;
    }

    /**
     * Create a macro configuration based on the given Java Bean configuration
     * 
     * @param macroNames
     *            the <code>pathMacroNames</code> configuration
     * @param macroValues
     *            the <code>pathMacroValue</code> configuration
     * 
     * @return a new macro handler
     */
    private List<PathMacro> determinePathMacros(
        final SparseArray<String> macroNames,
        final SparseArray<String> macroValues
    ) {
        final List<PathMacro> macros = new ArrayList<>();
        for (final ListIterator<String> nameIterator = macroNames.populationIterator(); nameIterator.hasNext();) {
            final Integer i = Integer.valueOf(nameIterator.nextIndex());
            final String macroName = nameIterator.next();
            final String macroValue = Objects.requireNonNull(
                macroValues.get(i),
                () -> "Missing path macro value with index " + i
            );
            macros.add(
                new PathMacro(
                    macroName,
                    macroValue
                )
            );
        }
        return macros;
    }

}
