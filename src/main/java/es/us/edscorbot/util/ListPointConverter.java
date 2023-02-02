package es.us.edscorbot.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import es.us.edscorbot.models.Point;



@Converter
public class ListPointConverter implements  AttributeConverter<List<Point>, String>{
    
    private static final String JOINT_REF_SEPARATOR = ",";
    private static final String POINT_SEPARATOR = ";";

    @Override
    public String convertToDatabaseColumn(List<Point> points) {
        StringBuilder sb = new StringBuilder();
        Iterator<Point> it = points.iterator();
        while(it.hasNext()){
            Point next = it.next();
            sb.append(convertToDatabaseColumn(next));
            if(it.hasNext()){
                sb.append(POINT_SEPARATOR);
            }
        }

        return sb.toString();
    }

    private String convertToDatabaseColumn(Point point){
        StringBuilder sb = new StringBuilder();

        sb.append(point.getJ1Ref());
        sb.append(JOINT_REF_SEPARATOR);
        sb.append(point.getJ2Ref());
        sb.append(JOINT_REF_SEPARATOR);
        sb.append(point.getJ3Ref());
        sb.append(JOINT_REF_SEPARATOR);
        sb.append(point.getJ3Ref());
        sb.append(JOINT_REF_SEPARATOR);

        return sb.toString();
    }

    @Override
    public List<Point> convertToEntityAttribute(String dbTrajectory) {
        ArrayList<Point> trajectory = new ArrayList<Point>();
        String[] points = dbTrajectory.split(POINT_SEPARATOR);
        for(String dbPoint: points){
            String[] jRefs = dbPoint.split(JOINT_REF_SEPARATOR);
            double j1Ref = Double.parseDouble(jRefs[0].trim());
            double j2Ref = Double.parseDouble(jRefs[1].trim());
            double j3Ref = Double.parseDouble(jRefs[2].trim());
            double j4Ref = Double.parseDouble(jRefs[3].trim());

            Point point =  new Point(j1Ref,j2Ref,j3Ref,j4Ref);

            trajectory.add(point);
        }
        return trajectory;
    }
}
