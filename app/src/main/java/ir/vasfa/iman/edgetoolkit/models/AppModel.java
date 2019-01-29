package ir.vasfa.iman.edgetoolkit.models;

import android.graphics.drawable.Drawable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppModel {
    private String name;
    private String packageName;
    private Drawable drawable;
    public AppModel(){

    }

    @Override
    public boolean equals(Object obj) {
        return (this.packageName.equals(((AppModel)obj).packageName));
    }
}
