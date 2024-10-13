package sk.stuba.pks.old.annotation;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.Retention;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Qualifier
public @interface DefaultOperationHandlerQualifier {
}
