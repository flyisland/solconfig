# Solace PS+ with TLS

## Reference

[Secrets Configuration](https://docs.solace.com/Configuring-and-Managing/SW-Broker-Specific-Config/Docker-Tasks/Config-Secrets.htm)

## Setup a PS+ broker with TLS enable

### Create a self signed certificate

```bash
# Generate a Private Key
$ openssl genrsa -out localhost.key 2048
Generating RSA private key, 2048 bit long modulus
......................+++
.....................................................................+++
e is 65537 (0x10001)

# Generate a CSR (Certificate Signing Request) with subjectAltName
$ openssl req -new -sha256 \
-out localhost.csr \
-key localhost.key \
-config openssl.cnf \
-subj "/C=CN/ST=GuangDong/L=ShenZhen/O=Acme, Inc./CN=localhost/"

# Generating a Self-Signed Certificate
$ openssl x509 -req \
    -sha256 \
    -days 365 \
    -in localhost.csr \
    -signkey localhost.key \
    -out localhost.crt \
    -extensions req_ext \
    -extfile openssl.cnf

Signature ok
subject=/C=CN/ST=GuangDong/L=ShenZhen/O=Acme, Inc./CN=localhost
Getting Private key

# Generate a PEM file for Solace PS+ broker
$ cat localhost.crt localhost.key > localhost.pem

# Check the CSR and Certificate, you should see "Subject Alternative Name"
$ openssl req -text -noout -in localhost.csr
$ openssl x509 -text -noout -in localhost.crt
```

### Create a PS+ docker instance with TLS enabled

Update the "volumes" section of `./docker-compose.yml` with the full path of the folder contains above certificate.

## Start the PS+ broker

```bash
docker-compose up -d
Creating network "tls_default" with the default driver
Creating tlsbroker ... done
```

## Verify the TLS service is enable

You will find that ports like `1943` (Access to PubSub+ Manager over HTTPS, SEMP over TLS), 55443 (SMF TLS / SSL) all open now.

```bash
docker exec -it tlsbroker /usr/sw/loads/currentload/bin/cli -A

Solace PubSub+ Standard Version 9.5.0.25

The Solace PubSub+ Standard is proprietary software of
Solace Corporation. By accessing the Solace PubSub+ Standard
you are agreeing to the license terms and conditions located at
http://www.solace.com/license-software

Copyright 2004-2020 Solace Corporation. All rights reserved.

To purchase product support, please contact Solace at:
https://solace.com/contact-us/

Operating Mode: Message Routing Node

3dd5cd886d54> show service

Msg-Backbone:       Enabled
  VRF:              management
  SMF:              Enabled
    Web-Transport:  Enabled
  REST Incoming:    Enabled
  REST Outgoing:    Enabled
  MQTT:             Enabled
  AMQP:             Enabled
  Health-check:     Enabled
  Mate-link:        Enabled
  Redundancy:       Enabled

Max Incoming Connections:       100
  Service SMF:                  100
  Service Web-Transport:        100
  Service REST:                 100
  Service MQTT:                 100
  Service AMQP:                 100
Max Outgoing Connections:
  Service REST:                 100
Max SSL Connections:            100

Event Threshold                           Set Value      Clear Value
---------------------------------- ---------------- ----------------
Incoming Connections                        80%(80)          60%(60)
  Service SMF                               80%(80)          60%(60)
Outgoing Connections
  Service REST                              80%(80)          60%(60)
SSL Connections                             80%(80)          60%(60)


Flags Legend:
TP - Transport
T+U - TCP and UDP
S - SSL (Y=Yes, N=No, -=not-applicable)
C - Compressed (Y=Yes, N=No, -=not-applicable)
R - Routing Ctrl (Y=Yes, N=No, -=not-applicable)
VRF - VRF (Mgmt=management, MsgBB=msg-backbone)
A - Admin State (U=Up, D=Down, -=not-applicable)
O - Oper State (U=Up, D=Down, -=not-applicable)

                                              Status
Service    TP  S C R VRF   MsgVpn          Port  A O Failed Reason
---------- --- ----- ----- --------------- ----- --- --------------------------
SEMP       TCP N - - Mgmt                   8080 U U
SEMP       TCP Y - - Mgmt                   1943 U U
SMF        TCP N N N Mgmt                  55555 U U
---Press any key to continue, or `q' to quit---
SMF        TCP N Y N Mgmt                  55003 U U
SMF        TCP N N Y Mgmt                  55556 U D
SMF        TCP Y N N Mgmt                  55443 U U
SMF        WEB N - - Mgmt                   8008 U U
SMF        WEB Y - - Mgmt                   1443 U U
MQTT       TCP N - - Mgmt  default          1883 U U
MQTT       TCP Y - - Mgmt  default          8883 U U
MQTT       WEB N - - Mgmt  default          8000 U U
MQTT       WEB Y - - Mgmt  default          8443 U U
AMQP       TCP N - - MsgBB default          5672 U U
AMQP       TCP Y - - MsgBB default          5671 U U
REST       WEB N - - Mgmt  default          9000 U U
REST       WEB Y - - Mgmt  default          9443 U U
MATELINK   TCP N N N Mgmt                   8741 U D Missing Mate Address
HEALTHCHK  TCP N N N Mgmt                   5550 U U
REDUNDANCY TCP Y N N Mgmt                   8300 U D
REDUNDANCY T+U Y N N Mgmt                   8301 U D
REDUNDANCY T+U Y N N Mgmt                   8302 U D

3dd5cd886d54>

```
