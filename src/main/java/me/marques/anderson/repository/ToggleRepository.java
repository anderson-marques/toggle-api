package me.marques.anderson.repository;

import io.vertx.core.Future;
import me.marques.anderson.domain.Toggle;

import java.util.List;

public interface ToggleRepository {

    Future<Toggle> insert(final Toggle toggle);

    Future<Toggle> update(final Toggle toggle);

    Future<List<Toggle>> listAll();

    Future<Toggle> findByName(final String name);

}