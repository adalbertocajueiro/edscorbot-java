package es.us.edscorbot.util;

import java.util.List;

import es.us.edscorbot.models.Point;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrajectoryDTO {
    private String email;
    private List<Point> points;
}
