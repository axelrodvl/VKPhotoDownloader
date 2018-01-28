package co.axelrod.vk.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * Created by Vadim Axelrod (vadim@axelrod.co) on 27.01.2018.
 */

@Getter
@Setter
@AllArgsConstructor
public class User {
    private Integer id;
    private String firstName;
    private String lastName;
    private Integer sex;
    private Integer photoCount;
    private ArrayList<Photo> photos;

    @Override
    public String toString() {
        return id + " " + firstName + " " + lastName + " " + sex + "\n";
    }
}
