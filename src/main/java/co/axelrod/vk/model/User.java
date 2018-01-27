package co.axelrod.vk.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
    private String screenName;
    private Integer sex;
    private Integer photoCount;
    private List<String> photoUrls;

    @Override
    public String toString() {
        return id + " " + firstName + " " + lastName + " " + screenName + " " + sex + "\n" + (photoUrls.isEmpty() ? "" : photoUrls.get(0)) + "\n" + "\n";
    }
}
