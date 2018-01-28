package co.axelrod.vk.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Vadim Axelrod (vadim@axelrod.co) on 27.01.2018.
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Photo {
    private String id;
    private String url;
    private Integer likes;

    @Override
    public String toString() {
        return id + " " + url + " " + likes;
    }
}
