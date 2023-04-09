package es.us.edscorbot.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import es.us.edscorbot.models.Point;



@Converter
public class ListPointConverter implements  AttributeConverter<List<Point>, String>{
    
    @Override
    public String convertToDatabaseColumn(List<Point> points) {

        Gson gson = new Gson();
        return gson.toJson(points);

    }

    @Override
    public List<Point> convertToEntityAttribute(String dbTrajectory) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Point>>(){}.getType();
        return gson.fromJson(dbTrajectory, listType);
    }
}
