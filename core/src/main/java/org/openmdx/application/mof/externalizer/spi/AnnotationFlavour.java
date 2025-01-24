package org.openmdx.application.mof.externalizer.spi;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import org.openmdx.application.mof.mapping.cci.ExtendedFormatOptions;
import org.openmdx.application.mof.mapping.java.StandardAnnotationRenderer;
import org.openmdx.application.mof.mapping.spi.MarkdownRendererFactory;

public enum AnnotationFlavour {
	
	STANDARD {
		
		@Override
		public void applyExtendedFormat(Collection<String> extendedFormats) {
			// default
		}

		@Override
		public Function<String, String> createRenderer() {
			return new StandardAnnotationRenderer();
		}

		@Override
		public Function<String, String> createRenderer(Supplier<Supplier<Function<String, String>>> factoryBuilder) {
			return Function.identity();
		}

	},
	MARKDOWN {
		
		@Override
		public void applyExtendedFormat(Collection<String> extendedFormats) {
			extendedFormats.add(EXTENDED_FORMAT);
		}

		@Override
		public Function<String, String> createRenderer() {
			return new MarkdownRendererFactory().get();
		}

		@Override
		public Function<String, String> createRenderer(Supplier<Supplier<Function<String, String>>> factoryBuilder) {
			return factoryBuilder.get().get();
		}
		
	};

	public static final AnnotationFlavour DEFAULT = STANDARD;
	private static final String EXTENDED_FORMAT = ExtendedFormatOptions.MARKDOWN;
	
	public abstract void applyExtendedFormat(Collection<String> extendedFormats);
	public abstract Function<String, String> createRenderer();
	public abstract Function<String, String> createRenderer(Supplier<Supplier<Function<String, String>>> factoryBuilder);
	
	public boolean isMarkdown() {
		return this == MARKDOWN;
	}

	public static AnnotationFlavour fromMarkdownOption(boolean markdown) {
		return markdown ? MARKDOWN : STANDARD; 
	}
	
	public static AnnotationFlavour fromExtendedFormats(Collection<String> extendedFormats) {
		return fromMarkdownOption(extendedFormats.remove(EXTENDED_FORMAT));
	}
	
}
