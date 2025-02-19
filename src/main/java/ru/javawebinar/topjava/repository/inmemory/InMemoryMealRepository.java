package ru.javawebinar.topjava.repository.inmemory;

import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.MealRepository;
import ru.javawebinar.topjava.util.MealsUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryMealRepository implements MealRepository {
    private final Map<Integer, Meal> mealsMap = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    {
        for (Meal meal : MealsUtil.meals) {
            save(meal, 1);
        }
    }

    @Override
    public Meal save(Meal meal, int userId) {
        if (meal.isNew()) {
            meal.setId(counter.incrementAndGet());
            meal.setUserId(userId);
            mealsMap.put(meal.getId(), meal);
            return meal;
        }
        // handle case: update, but not present in storage
        return mealsMap.computeIfPresent(meal.getId(), (id, oldMeal) -> oldMeal.getUserId().equals(userId) ? meal : oldMeal);
    }

    @Override
    public boolean delete(int id, int userId) {
        return mealsMap.computeIfPresent(id, (k, v) -> v.getUserId().equals(userId) ? null : v) != null;
    }

    @Override
    public Meal get(int id, int userId) {
        Meal meal = mealsMap.get(id);
        return meal.getUserId().equals(userId) ? meal : null;
    }

    @Override
    public List<Meal> getAll(int userId) {
        return mealsMap.values()
                .stream()
                .filter(m -> m.getUserId().equals(userId))
                .sorted(Comparator.comparing(Meal::getDateTime).reversed())
                .collect(Collectors.toList());
    }
}

