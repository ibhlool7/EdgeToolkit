package ir.vasfa.iman.edgetoolkit.models;

import android.graphics.Bitmap;

import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactModel {
    private String name;
    private String number;
    private Bitmap photo;
    private String id;

    @Override
    public boolean equals(Object obj) {
         return this.id.equals(((ContactModel)obj).id);
    }
}
