package net.yury.simplehttp.annotation;

public @interface SHMethod {
    public String name() default "GET";
}
