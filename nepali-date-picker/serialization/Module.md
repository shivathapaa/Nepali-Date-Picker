# Module serialization

Optional `kotlinx-serialization` support for the `:core` calendar models. Adds `KSerializer`
instances so `CustomCalendar`, `SimpleDate`, `SimpleTime`, and the other model types cross process
and storage boundaries without a hand-written mapping layer. Pure Kotlin, no Compose — ships on the
same expanded target matrix as `:core`.

# Package dev.shivathapaa.nepalidatepickerkmp.serialization

Serializers for the core calendar models. Register or reference these when persisting or
transmitting Nepali Date Picker types through a `kotlinx-serialization` format.
