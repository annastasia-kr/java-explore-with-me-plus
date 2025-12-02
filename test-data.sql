-- Очистка таблиц
DELETE FROM participation_requests;
DELETE FROM events;
DELETE FROM categories;
DELETE FROM users;

-- Сброс последовательностей
ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE categories_id_seq RESTART WITH 1;
ALTER SEQUENCE events_id_seq RESTART WITH 1;
ALTER SEQUENCE participation_requests_id_seq RESTART WITH 1;

-- Тестовые пользователи
INSERT INTO users (name, email, created_date) VALUES
('Иван Иванов', 'ivan@example.com', NOW()),
('Петр Петров', 'petr@example.com', NOW()),
('Анна Сидорова', 'anna@example.com', NOW());

-- Тестовые категории
INSERT INTO categories (name) VALUES
('Концерты'),
('Выставки'),
('Спорт');

-- Тестовые события
INSERT INTO events (title, annotation, description, category_id, event_date, initiator_id,
                   paid, participant_limit, request_moderation, state, created_date, published_date) VALUES
('Концерт рок-группы', 'Отличный концерт', 'Описание концерта',
 1, NOW() + INTERVAL '7 days', 1, true, 100, true, 'PUBLISHED', NOW(), NOW()),
('Фотовыставка', 'Выставка фотографий', 'Работы фотографов',
 2, NOW() + INTERVAL '5 days', 2, false, 50, false, 'PUBLISHED', NOW(), NOW()),
('Мастер-класс', 'Изучение технологий', 'Практический мастер-класс',
 3, NOW() + INTERVAL '10 days', 3, true, 20, true, 'PUBLISHED', NOW(), NOW());

-- Тестовые запросы на участие
INSERT INTO participation_requests (event_id, requester_id, status, created_date) VALUES
(1, 2, 'CONFIRMED', NOW()),
(1, 3, 'PENDING', NOW()),
(2, 1, 'CONFIRMED', NOW()),
(3, 2, 'REJECTED', NOW());