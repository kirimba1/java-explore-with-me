package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.EmailAlreadyExistsException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequestDto;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getUsers(List<Integer> ids, Integer from, Integer size) {

        if (ids == null) {
            Pageable pageable = PageRequest.of(from / size, size);
            log.info("Получение всех пользователей. from={}, size={}", from, size);

            return userRepository.findAll(pageable)
                    .map(userMapper::toDto)
                    .toList();
        }

        log.info("Получение пользователей по ID: {}", ids);

        return userRepository.findAllByIdIn(ids)
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public UserDto addUser(NewUserRequestDto newUserRequestDto) {
        log.info("Добавление нового пользователя: {}", newUserRequestDto);

        if (userRepository.existsByEmail(newUserRequestDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email уже используется");
        }

        return userMapper.toDto(userRepository.save(userMapper.toEntity(newUserRequestDto)));
    }

    @Override
    public void deleteUserById(Long userId) {
        log.info("Удаление пользователя: {}", userId);

        if (userRepository.existsById(userId)) {
            log.info("Пользователь удален");
            userRepository.deleteById(userId);
        } else {
            log.info("Пользователь не найден");
            throw new NotFoundException("Пользователь с id:" + userId + " не найден");
        }

    }
}
