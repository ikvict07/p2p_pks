local my_protocol = Proto("IKCP", "Custom UDP Protocol")

my_protocol.fields.session_id = ProtoField.bytes("IKCP.session_id", "Session ID")
my_protocol.fields.sequence_number = ProtoField.uint32("IKCP.sequence_number", "Sequence Number", base.DEC)
my_protocol.fields.flags = ProtoField.uint8("IKCP.flags", "Flags", base.HEX)
my_protocol.fields.ack_flag = ProtoField.uint8("IKCP.ack_flag", "Ack Flag", base.DEC, {
    [0] = "None",
    [1] = "Ack",
    [2] = "Syn",
    [3] = "SynAck"
}, 0x03)

my_protocol.fields.payload_type = ProtoField.uint8("IKCP.payload_type", "Payload Type", base.DEC, {
    [0] = "Data",
    [1] = "KeepAlive",
    [2] = "FIN"
}, 0x0C)
my_protocol.fields.payload_length = ProtoField.uint16("IKCP.payload_length", "Payload Length", base.DEC)
my_protocol.fields.payload = ProtoField.bytes("IKCP.payload", "Payload")
my_protocol.fields.checksum = ProtoField.uint32("IKCP.checksum", "Checksum", base.HEX)


function my_protocol.dissector(buffer, pinfo, tree)
    pinfo.cols.protocol = my_protocol.name

    local subtree = tree:add(my_protocol, buffer(), "Custom UDP Protocol")

    if buffer:len() >= 4 then
        subtree:add(my_protocol.fields.session_id, buffer(0, 4))
    end
    if buffer:len() >= 8 then
        subtree:add(my_protocol.fields.sequence_number, buffer(4, 4):le_uint())
    end
    if buffer:len() >= 9 then
        local flags = buffer(8, 1):uint()

        local ack_flag = bit.band(flags, 0x03)

        local payload_type = bit.band(flags, 0x0C)

        local flags_tree = subtree:add(my_protocol.fields.flags, buffer(8, 1))
        flags_tree:add(my_protocol.fields.ack_flag, ack_flag)
        flags_tree:add(my_protocol.fields.payload_type, payload_type)

        if flags == 0 then
            pinfo.cols.info:set("Message")
        end
    end

    if buffer:len() >= 11 then
        subtree:add(my_protocol.fields.payload_length, buffer(9, 2):uint())
    end

    local payload_length = 0
    if buffer:len() >= 11 then
        payload_length = buffer(9, 2):uint()
    end

    if payload_length > 0 and buffer:len() >= (11 + payload_length) then
        subtree:add(my_protocol.fields.payload, buffer(11, payload_length))
    end

    if buffer:len() >= (11 + payload_length + 4) then
        subtree:add(my_protocol.fields.checksum, buffer(11 + payload_length, 4))
    end

    local messages = "IKCP.flags == 0"
    set_color_filter_slot(7, messages)
    local keepAlive = "IKCP.payload_type == 1"
    set_color_filter_slot(9, keepAlive)


    return buffer:len()
end

function my_protocol_heuristic(buffer, pinfo, tree)
    if (pinfo.dst_port >= 7000 and pinfo.dst_port <= 8000) or
            (pinfo.src_port >= 7000 and pinfo.src_port <= 8000) then
        my_protocol.dissector(buffer, pinfo, tree)
        return true
    end

    if buffer:len() < 15 then
        return false
    end

    local payload_length = buffer(9, 2):uint()
    if payload_length > 1476 then
        return false
    end

    my_protocol.dissector(buffer, pinfo, tree)
    return true
end

my_protocol:register_heuristic("udp", my_protocol_heuristic)
