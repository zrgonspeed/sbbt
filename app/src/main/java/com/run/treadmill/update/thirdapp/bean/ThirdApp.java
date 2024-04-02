package com.run.treadmill.update.thirdapp.bean;

public class ThirdApp {
    public final String name;
    public final String packageName;
    public final String viewName;
    public final int home_drawable;
    public final int run_drawable;
    public final int update_drawable;

    public static class Builder {
        private String name = "";
        private String viewName = "";
        private String packageName = "";
        private int home_drawable = 0;
        private int run_drawable = 0;
        private int update_drawable = 0;

        public Builder(String name, String packageName) {
            this.name = name;
            this.viewName = name;
            this.packageName = packageName;
        }

        public Builder homeDrawable(int val) {
            home_drawable = val;
            return this;
        }

        public Builder viewName(String val) {
            viewName = val;
            return this;
        }

        public Builder runDrawable(int val) {
            run_drawable = val;
            return this;
        }

        public Builder updateDrawable(int val) {
            update_drawable = val;
            return this;
        }

        public ThirdApp build() {
            return new ThirdApp(this);
        }
    }

    private ThirdApp(Builder builder) {
        name = builder.name;
        packageName = builder.packageName;
        viewName = builder.viewName;
        home_drawable = builder.home_drawable;
        run_drawable = builder.run_drawable;
        update_drawable = builder.update_drawable;
    }
}
