package org.openmdx.application.mof.externalizer.spi;

public class ExternalizationConfiguration {

    public ExternalizationConfiguration(
    ){
        this(
            AnnotationFlavour.DEFAULT,
            JakartaFlavour.DEFAULT,
            ChronoFlavour.DEFAULT,
            ExternalizationScope.DEFAULT
        );
    }

    public ExternalizationConfiguration(
        AnnotationFlavour annotationFlavour,
        JakartaFlavour jakartaFlavour,
        ChronoFlavour chronoFlavour,
        ExternalizationScope externalizationScope
    ) {
        this.annotationFlavour = annotationFlavour;
        this.jakartaFlavour = jakartaFlavour;
        this.chronoFlavour = chronoFlavour;
        this.externalizationScope = externalizationScope;
    }

    /**
     * Tells whether annotations use markdown
     */
    public final AnnotationFlavour annotationFlavour;

    /**
     * Tells whether Jakarta 8 or a contemporary flavour shall be used
     */
    public final JakartaFlavour jakartaFlavour;

    /**
     * Tells whether the classic or the contemporary JMI API shall be used
     */
    public final ChronoFlavour chronoFlavour;

    /**
     * Tells whether provided packages shall be exported, too
     */
    public final ExternalizationScope externalizationScope;

}
