package org.openmdx.application.mof.externalizer.spi;

import org.openmdx.application.mof.mapping.cci.ExtendedFormatOptions;

import java.util.Collection;

public enum ExternalizationScope {

    /**
     * Provided packages are excluded from the {@code STANDARD} selection
     */
    STANDARD {

        @Override
        public void applyExtendedFormat(Collection<String> extendedFormats) {
            // default
        }

    },

    /**
     * Provided packages are included in the {@code EXTENDED} selection
     */
    EXTENDED {

        @Override
        public void applyExtendedFormat(Collection<String> extendedFormats) {
            extendedFormats.add(EXTENDED_FORMAT);
        }

    };

    public boolean includeProvidedPackages(){
        return this == EXTENDED;
    }

    private static final String EXTENDED_FORMAT = ExtendedFormatOptions.INCLUDE_PROVIDED_PACKAGES;
    public static final ExternalizationScope DEFAULT = STANDARD;

    public abstract void applyExtendedFormat(Collection<String> extendedFormats);

    private static ExternalizationScope fromIncludeProvidedPackagesOption(boolean includeProvidedPackages) {
        return includeProvidedPackages ? EXTENDED : STANDARD;
    }

    public static ExternalizationScope fromExtendedFormats(Collection<String> extendedFormats) {
        return fromIncludeProvidedPackagesOption(extendedFormats.remove(EXTENDED_FORMAT));
    }

}
