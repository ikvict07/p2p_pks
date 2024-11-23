# Ikvict Communication Protocol

I created a program that uses my own protocol over udp.

[Github](https://github.com/ikvict07/p2p_pks.git)

The program supports two modes: CLI and GUI [see How to run](#how-to-run)

---

## Table of content

- [Connection creating](#connection-creating)
- [Message sending](#message-sending)
- [Keep-Alive](#keep-alive)
- [Corrupted message](#sending-a-corrupted-message)
- [Closing connection](#closing-the-connection)
- [How to run](#how-to-run)
- [How to use](#how-to-use)
- [Protocol](#protocol)
- [Speed](#speed)

---

## Connection creating

| ![Syn](static/Syn.png) |
|:----------------------:|
|     *Sending syn*      |

| ![SynAck](static/SynAck.png) |
|:----------------------------:|
|       *Confirming syn*       |

| ![SynAck](static/AckSynAck.png) |
|:-------------------------------:|
|     *Confirming SynAckSyn*      |

<p style="text-align: center;">
  Connection established
</p>

---

## Message sending

| ![Message](static/SendMessageUI.png) |
|:------------------------------------:|
|          *Sending message*           |

| ![MessageSent](static/SentMessage.png) |
|:--------------------------------------:|
|             *Message sent*             |

| ![MessageConfirmed](static/MessageConfirmed.png) |
|:------------------------------------------------:|
|               *Message confirmed*                |

| ![MessageReceived](static/MessageReceived.png) |
|:----------------------------------------------:|
|               *Message received*               |

---

## Keep-alive

| ![KeepAliveSent](static/KeepAliveSent.png) |
|:------------------------------------------:|
|             *Keep-alive sent*              |

| ![KeepAliveConfirmed](static/KeepAliveConfirmed.png) |
|:----------------------------------------------------:|
|                *Keep-alive confirmed*                |

---

## Sending a corrupted message

| ![Message](static/SendingCorrupted.png) |
|:---------------------------------------:|
|       *Sending corrupted message*       |

| ![MessageSent](static/CorruptedSent.png) |
|:----------------------------------------:|
|              *Message sent*              |

| ![Message](static/Resend.png) |
|:-----------------------------:|
|      *Resending message*      |

| ![MessageConfirmed](static/CorrupdetConfirm.png) |
|:------------------------------------------------:|
|               *Message confirmed*                |

| ![MessageReceived](static/CorruptedReceived.png) |
|:------------------------------------------------:|
|                *Message received*                |

---

## Closing the connection

| ![U1_1](static/User1Close.png) |
|:------------------------------:|
|    *U1 closing connection*     |

| ![U1_2](static/U1CloseSent.png) |
|:-------------------------------:|
|           *FIN sent*            |

| ![U1_3](static/U2ConfirmedCloseFromU1.png) |
|:------------------------------------------:|
|             *U2 confirms FIN*              |

| ![U2_1](static/U2Close.png) |
|:---------------------------:|
|   *U2 closing connection*   |

| ![U2_2](static/U2Sent.png) |
|:--------------------------:|
|         *FIN sent*         |

| ![U2_3](static/U1ConfirmedCloseFromU2.png) |
|:------------------------------------------:|
|             *U1 confirms FIN*              |

<p style="text-align: center;">
  Connection closed
</p>

---

## How to run

#### From console run

`java -jar pks-1.0.0.jar`

---

#### How to configure

`java -jar /path/to/jar/pks-1.0.0.jar --key=value --key2=value2`

Where key can be any of:

`logging.level.root` (`off`, `info`, `trace`) – **_use `trace` to see all info about packets_**

`socket.max-payload-size` (1-800)

`socket.message-resending-confirmation-time-ms`

`socket.message-resending-frequency-ms`

`socket.keep-alive-frequency-ms`

`socket.connection-timeout-ms`

`socket.attempts-to-reconnect`

`socket.retry-to-connect-every-ms`

`general.file-save-location`

`ui.enabled` (`true`, `false`)

---

They aren't mandatory as they have default values,
However, I recommend changing these:

`general.file-save-location`
and
`ui.enabled`

---

## How to use

### CLI mode

If you set `ui.enabled=false`
the program will run in CLI mode

The Console will ask you:

- If you want to open ports for listening
- How many you want to open
- List of ports


- If you want to connect to someone
- How many connections you want
- Remote port to connect to
- Remote Ip to connect to
- Port you will open for listening

Now the program is configured and you can use it

- Choose `ip:port` from the list of available connections
    - or `change` – to configure packet fragment size
- Select a type of message you want to send (`message`, `file`, `corrupted`)
    - or `close` if you want to close this connection
- Write a message
    - or file path

### GUI mode

If you set `ui.enabled=true`
the program will run in GUI mode

All the configuration now will be done using GUI

Sending and reading messages will be done using GUI as well

---

## Protocol

![Header](static/ProtHeader-1.png)

### Keep-Alive

Every `N` seconds node sends an empty packet with a keep-alive flag

If this node doesn't receive packet Ack keep-alive within `x` seconds `y` times

It will automatically disconnect

*`N`, `x`, `y` – are configuration params*

### Receiving confirmation

After Node2 receives a message from Node1, it must send an empty packet with `SequenceNumber` set to the same value as
a received packet and with `ack` flag

If Node1 doesn't receive this ack packet within a time interval that is configurable, the message is considered as non
received and will be resented. 

### Closing connection

The Connection will be closed when both side receive FIN packet

---

## Speed

It takes ~20s to read, send and store 100 mb file