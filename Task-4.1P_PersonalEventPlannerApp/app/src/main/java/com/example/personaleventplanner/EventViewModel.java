package com.example.eventplanner;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class EventViewModel extends AndroidViewModel {

    private final EventRepository repository;
    public final LiveData<List<EventEntity>> allEvents;

    public EventViewModel(Application application) {
        super(application);
        repository = new EventRepository(application);
        allEvents  = repository.getAllEvents();
    }

    public LiveData<EventEntity> getEventById(int id) {
        return repository.getEventById(id);
    }

    public void insert(EventEntity event) {
        repository.insert(event);
    }

    public void update(EventEntity event) {
        repository.update(event);
    }

    public void delete(EventEntity event) {
        repository.delete(event);
    }
}
