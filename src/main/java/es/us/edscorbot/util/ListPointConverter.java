package es.us.edscorbot.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;
import java.util.LinkedList;

import com.google.gson.Gson;

import es.us.edscorbot.models.Point;



@Converter
public class ListPointConverter implements  AttributeConverter<List<Point>, String>{
    
    @Override
    public String convertToDatabaseColumn(List<Point> points) {
        Gson gson = new Gson();
        return gson.toJson(points);
    }

    @Override
    public List<Point> convertToEntityAttribute(String json) {
        Gson gson = new Gson();
        return (List<Point>) gson.fromJson(json, LinkedList.class);
    }
}
