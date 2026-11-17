package com.habibah.springapp.repository;

import com.habibah.springapp.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findAllByUserEmailOrderByDefaultAddressDescIdDesc(String email);

    Optional<Address> findByIdAndUserEmail(Long id, String email);

    boolean existsByUserEmail(String email);

    Optional<Address> findByUserEmailAndDefaultAddressTrue(String email);
}
