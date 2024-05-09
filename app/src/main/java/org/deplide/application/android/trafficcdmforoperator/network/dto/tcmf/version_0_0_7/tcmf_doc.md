# Transport Collaboration Messaging Format

This document describes the Transport Collaboration Format (TCMF) in detail. **NOTE!** This document is written for TCMF version 0.0.7 and some things might change in later versions.

## Background and purpose

The TCMF is derived from the previous formats Port Call Messaging Format (PCMF) and Railway Collaboration Messaging Format (RCMF) in order
to support any type of transportation, including multi-modal transports. It can be used to describe more or less any situation within an end-to-end transport.

## State catalogue

Because of the liberal nature of the format and a lot of fields that can have arbitrary parts, there is often the need to use a state catalogue. A state catalogue defines "legal" combinations of values for different timestamps. For example, in a train-only journey, no other identifier than `tcmf:train...` might be allowed.

It also serves as a standardized way to express situations. If there are no state catalogue present, some users might report a station like `tcmf:station:Mgb` and another one `tcmf:train_station:Mgb` and they will not end up representing the same thing.

## Simple example

```json
{
  "payload": {
    "@type": "LocationState",
    "timeType": "planned",
    "referenceObject": "tcmf:reference_object:train:train_id:testtåget",
    "timeSequence": "departed_from",
    "location": "tcmf:location:station:Mgb:testspåret",
    "time": "2024-03-18T12:24:00.000Z"
  },
  "grouping": ["tcmf:grouping:train:train_id:testtåget"],
  "messageId": "tcmf:message:8463495a-027e-49c4-aaa3-4969f168f642",
  "reportedAt": "2024-03-18T08:25:00.797Z",
  "reportedBy": "tcmf:user:RISE:pontus_dev",
  "version": "0.0.7"
}
```

This example describes train "testtåget" departing from Malmö Freight Yard.

The complete schema of version 0.0.7 can be found in the file `transport-collaboration-message.json`.

## Identifiers using URN syntax

For many values, a syntax is used to allow the sender to specify different levels of details for a value. For example, the location field of the payload must match the following syntax: `tcmf:location:<location_type>:<location>` where location can be a simple string, but also an URN in itself, to specify train track for example.

A berth in a port could be described like this: `tcmf:location:port:SEGOT:71:a`. This is very practical when some data sources provide a more detailed value than others, which can then be combined.

In some cases, extra labels can be added like so `tcmf:location:station:Mgb:track:23` but this is not mandatory by the format as of version 0.0.7, although that syntax is used for some data sources.

## The structure

A message following the TCMF has the following parts:

- Metadata
- Payload
- Grouping

The format does not mention anything about the order of these, just that they need to be included.

## Metadata

The metadata consists of four mandatory fields and one optional. They are described in detail below.

### `messageId`

Unique identifier for the message following the syntax `tcmf:message:<UUID v4>`.

### `reportedAt`

When the message was reported. If the message is created from an external source, the external `reportedAt` should be used. If not, it is set to when the message was created.

### `reportedBy`

Who reported the timestamp. Must be a TCMF user defined as `tcmf:user:<company>:<username>` where `<username>` is optional.

### `version`

The TCMF version of the message. Used in order to mix different versions of timestamps on the same Kafka topic, for example.

### `source`

The only optional metadata field. It is used to indicate the original ID if the timestamp is generated from another type of timestamp and it has an ID. This can be useful if there are more data fields than the TCMF can support. The source can then be used to look up the original message and extract the extra data.

## Common payload fields

There are many fields that are common for several payloads. They are described below, in order to better understand the different payload types.

### `@type`

Must be hardcoded to one of the following strings:

```
LocationState
ServiceState
AdministrativeState
CarrierState
AttributeState
MessageOperation
```

Used to easily determine payload type in JSON formats etc.

### `time`

The actual timestamp in [ISO 8601 format](https://en.wikipedia.org/wiki/ISO_8601).

### `timeType`

The time type of the payload which can be either

- `planned` - for planned happenings in the future
- `estimated` - for estimations of when something will happen (ETA for example)
- `actual` - for describing timestamps happening _right now_ or in the past (ATA, ATD etc)

### `timeSequence`

Describing what sequence the timestamp describes, such as arrival, commencing service, setting attributes etc. Every payload type (except Message Operation) has their own `timeSequence`.

### `referenceObject`

_What_ the timestamp is about. Slightly depends on payload type, but as a general rule is what object the timestamp is describing.

## Payload and payload types

The payload describes the content of the timestamp. In order to describe different states with a timestamp, there exist several different payload types, each describing its own state type. They all have slightly different fields, which is described below. The common fields are described more in detail above.

### LocationState

Describes movement of a reference object at a certain place and time. Has the following required fields:

- `@type`
- `time`
- `location`
- `timeType`
- `timeSequence` for LocationState, valid time sequences are:
  - `arrived_to` for arrivals
  - `departed_from` for departures
  - `passed_by` for an arrival and immediate departure without stop
- `referenceObject`

There are no optional fields for the LocationState payload type.

### ServiceState

Describes a service commenced or completed for a reference object. The payload type has a unique field called `service`. This could either identify a generic service such as `tcmf:service:catering` or provide an identifier for a service, if it has one, such as `tcmf:service:catering:abc123`.

It has the following required fields:

- `@type`
- `time`
- `timeType`
- `timeSequence` for ServiceState, valid time sequences are:
  - `commenced`
  - `completed`
- `service` identifier for the service that is being performed. See examples above.

Optional fields are:

- `location`
- `referenceObject`

Reference object is optional since it is often very clear what the reference object is by the timestamp. Location can also be added do distinguish between identical services provided but at different locations.

### AdministrativeState

Describe some administrative state, which is often similar to service, but has no time type. An example could be `train_journey_cancelled`, where it is irrelevant to provide a time type. A train journey is either cancelled, or not cancelled and should for example not be estimated. Other than the lack of a time type it is very similar to the ServiceState payload type.

It has the following required fields:

- `@type`
- `time`
- `timeSequence` for ServiceState, valid time sequences are:
  - `requested`
  - `request_received`
  - `confirmed`
  - `cancelled`
  - `denied`
  - `assigned`
- `service`

Optional fields are:

- `location`
- `referenceObject`

### CarrierState

CarrierState is what makes the TCMF useful for describing multi-modal transports. It is used to describe that a reference object can now be followed by recieving timestamps of its carrier. It is most commonly used do described something being loaded on to something else, but since that is now always the case, we say that the reference object is _bound to_ its carrier. For example, a container loaded onto a truck would have `referenceObject: tcmf:reference_object:cargo_carrier:container_numder:abc123` and carrier `tcmf:carrier:truck:registration_plane:abc124`. Another example is when a wagon is connected to a train. Then the train can be followed, in order to see where the wagon is. Such a timestamp could look like below:

```json
{
  "payload": {
    "@type": "CarrierState",
    "timeType": "planned",
    "referenceObject": "tcmf:reference_object:wagon:vehicle_number:912345678912",
    "timeSequence": "bound_to",
    "carrier": "tcmf:carrier:train:train_id:Testtåget3",
    "time": "2024-03-19T13:38:00.000Z"
  },
  "grouping": [
    "tcmf:grouping:train:train_id:Testtåget3",
    "tcmf:grouping:wagon:vehicle_number:912345678912"
  ],
  "messageId": "tcmf:message:51c173f6-441c-4ed2-98e4-26fb2f3cded2",
  "reportedAt": "2024-03-19T12:39:14.666Z",
  "reportedBy": "tcmf:user:UNKNOWN:pontus_dev",
  "version": "0.0.7"
}
```

The CarrierState payload type has the following required fields:

- `@type`
- `time`
- `timeType`
- `carrier` what the reference object is being bound or unbound to/from
- `timeSequence` could be one of:
  - `bound_to`
  - `unbound_from`
- `referenceObject` what is being bound or unbound to/from a carrier

And the optional field:

- `location` which is often relevant but not always

### AttributeState

AttributeState is used to describe any attribute attributed to a reference object, at any point in time and possibly space. It has the somewhat different timesequences `set` and `unset`. If an attribute is `set`, one can assume that the attribute is applied until its explicitly `unset` again.

Both very static attributes can be described, such as container number or registration plate but also more dynamic ones can be described such as draft or length of a train.

As an example, the planned length of a train could be described with the below example:

```json
{
  "payload": {
    "@type": "AttributeState",
    "timeType": "planned",
    "referenceObject": "tcmf:reference_object:train:train_ud:1234",
    "timeSequence": "set",
    "attribute": "tcmf:attribute:train_length:100",
    "time": "2024-03-19T13:38:00.000Z"
  },
  "grouping": ["tcmf:grouping:train:train_id:1234"],
  "messageId": "tcmf:message:51c173f6-441c-4ed2-98e4-26fb2f3cded2",
  "reportedAt": "2024-03-19T12:39:14.666Z",
  "reportedBy": "tcmf:user:UNKNOWN:pontus_dev",
  "version": "0.0.7"
}
```

While the format itself would allow it, some attributes might not make sense to "estimate", such as a ship's name, but the combination of fields is determined by the state catalogue.

The AttributeState payload type has the following required fields:

- `@type`
- `time`
- `timeType`
- `attribute` what attribute to set or unset
- `timeSequence` could be one of:
  - `set`
  - `unset`
- `referenceObject` on what the attribute is set or unset

And the optional field:

- `location` makes sense for dynamic attributes but not for static ones

### MessageOperation

MessageOperation is a special payload type used only for modifying previous messages. It has, as of version 0.0.7, only the purpose to invalidate previous messages by referring to the message ID of the message to invalidate.

Example of invalidation of a message:

```json
{
  "payload": {
    "@type": "MessageOperation",
    "messageId": "tcmf:message:51c173f6-441c-4ed2-98e4-26fb2f3cded6",
    "operation": "invalidate"
  },
  "grouping": ["tcmf:grouping:train:train_id:1234"],
  "messageId": "tcmf:message:51c173f6-441c-4ed2-98e4-26fb2f3cded2",
  "reportedAt": "2024-03-19T12:39:14.666Z",
  "reportedBy": "tcmf:user:UNKNOWN:pontus_dev",
  "version": "0.0.7"
}
```

The MessageOperation payload type has the following required fields:

- `@type`
- `operation` currently only supports `invalidate`

It has no optional fields.

## Grouping

The grouping is an array of strings representing for whom the timestamp could be relevant. In practice, the grouping decides on which timelines a timestamp will be visible. This can be used to allow the timestamp to be seen in different contexts _or_ to allow for many different types of identifiers for the same reference object.

### Using grouping to model many identifiers for the same reference object

As an example, a ship can be identified using many identifiers. Some examples are MMSI, IMO, the ship's name and callsign. Some receivers of timestamps might listen only to one or a few of these identifiers and might miss timestamps if only the IMO is used, for example. For that reason, senders should always add as many groupings as they can provide in order to maximise the change of someone receiving it. An example grouping for a ship could be:

```json
{
  ...
  "grouping": [
    "tcmf:grouping:ship:imo:12345678",
    "tcmf:grouping:ship:mmsi:123456789",
    "tcmf:grouping:ship:callsign:HEJ123",
    "tcmf:grouping:ship:name:Acropolis",
  ]
  ...
}
```

### Using grouping to send timestamp to many receivers

There can be some timestamps that are relevant for more than one reference object. The most common example would be CarrierState messages. The `bind_to` or `unbound_from` message is probably relevant both for the reference object that is being carried and its carrier. As an example, when connecting a wagon to a train, the timestamp is probably relevant to both the wagon's timeline and the train's.

The wagon's timeline in order to know how to follow it - since it is bound to a train, follow the train. The timestamp could also be added to the train's timeline in order to keep track of how many wagons are currently on the train, for example.

In that CarrierState, the grouping field could look something like:

```json
{
  ...
  "grouping": [
    "tcmf:grouping:wagon:vehicle_number:123456789012",
    "tcmf:grouping:train:train_id:1234"
  ],
  "payload": {
    "@type": "CarrierState",
    ...
  }
  ...
}
```
