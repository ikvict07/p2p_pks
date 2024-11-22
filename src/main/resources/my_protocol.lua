-- Создание нового dissector для пользовательского протокола
local my_protocol = Proto("MyProtocol", "Custom UDP Protocol")

-- Определение полей протокола
my_protocol.fields.session_id = ProtoField.bytes("myprotocol.session_id", "Session ID")
my_protocol.fields.sequence_number = ProtoField.uint32("myprotocol.sequence_number", "Sequence Number", base.DEC)
my_protocol.fields.flags = ProtoField.uint8("myprotocol.flags", "Flags", base.HEX)
my_protocol.fields.ack_flag = ProtoField.uint8("myprotocol.ack_flag", "Ack Flag", base.DEC, {
    [0] = "None",
    [1] = "Ack",
    [2] = "Syn",
    [3] = "SynAck"
}, 0x03) -- Нижние 2 бита для ackFlag

my_protocol.fields.payload_type = ProtoField.uint8("myprotocol.payload_type", "Payload Type", base.DEC, {
    [0] = "Data",
    [1] = "KeepAlive",
    [2] = "FIN"
}, 0x0C) -- Следующие 2 бита для payloadType
my_protocol.fields.payload_length = ProtoField.uint16("myprotocol.payload_length", "Payload Length", base.DEC)
my_protocol.fields.payload = ProtoField.bytes("myprotocol.payload", "Payload")
my_protocol.fields.checksum = ProtoField.uint32("myprotocol.checksum", "Checksum", base.HEX)

-- Функция для анализа пакетов
function my_protocol.dissector(buffer, pinfo, tree)
    pinfo.cols.protocol = my_protocol.name

    -- Добавление протокола в дерево анализа
    local subtree = tree:add(my_protocol, buffer(), "Custom UDP Protocol")

    -- Проверка минимальной длины перед извлечением полей
    if buffer:len() >= 4 then
        subtree:add(my_protocol.fields.session_id, buffer(0, 4))
    end
    if buffer:len() >= 8 then
        subtree:add(my_protocol.fields.sequence_number, buffer(4, 4):le_uint())
    end
    if buffer:len() >= 9 then
        local flags = buffer(8, 1):uint()

        -- Извлечение нижних 2 бит для ack_flag
        local ack_flag = bit.band(flags, 0x03)  -- 0x03 = 00000011

        -- Извлечение следующие 2 бита для payload_type
        local payload_type = bit.band(flags, 0x0C)

        -- Добавляем данные в дерево
        local flags_tree = subtree:add(my_protocol.fields.flags, buffer(8, 1))
        flags_tree:add(my_protocol.fields.ack_flag, ack_flag)
        flags_tree:add(my_protocol.fields.payload_type, payload_type)

        -- Выделяем пакеты с ack_flag == 0
        if flags == 0 then
            pinfo.cols.info:set("Message")
        end
    end

    -- Добавление других полей, если есть
    if buffer:len() >= 11 then
        subtree:add(my_protocol.fields.payload_length, buffer(9, 2):uint())
    end

    local payload_length = 0
    if buffer:len() >= 11 then
        payload_length = buffer(9, 2):uint()
    end

    -- Проверка, что длина payload в пределах допустимого
    if payload_length > 0 and buffer:len() >= (11 + payload_length) then
        subtree:add(my_protocol.fields.payload, buffer(11, payload_length))
    end

    -- Проверка, что checksum можно считать
    if buffer:len() >= (11 + payload_length + 4) then
        subtree:add(my_protocol.fields.checksum, buffer(11 + payload_length, 4))
    end

    local messages = "myprotocol.flags == 0"
    set_color_filter_slot(7, messages)
    local keepAlive = "myprotocol.payload_type == 1"
    set_color_filter_slot(9, keepAlive)


    return buffer:len()  -- Возвращаем длину обработанного пакета
end



-- Эвристическая функция для проверки, является ли это наш протокол
function my_protocol_heuristic(buffer, pinfo, tree)
    if (pinfo.dst_port >= 7000 and pinfo.dst_port <= 8000) or
            (pinfo.src_port >= 7000 and pinfo.src_port <= 8000) then
        -- Разбираем пакет
        my_protocol.dissector(buffer, pinfo, tree)
        return true
    end

    if buffer:len() < 15 then
        return false -- Слишком короткий для нашего протокола
    end

    local payload_length = buffer(9, 2):uint()
    if payload_length > 1476 then
        return false -- Некорректная длина payload
    end

    -- Подходит, разбираем пакет
    my_protocol.dissector(buffer, pinfo, tree)
    return true
end

-- Регистрация эвристического dissector для UDP
my_protocol:register_heuristic("udp", my_protocol_heuristic)
