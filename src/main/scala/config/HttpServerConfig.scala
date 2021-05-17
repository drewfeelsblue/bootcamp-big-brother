package config

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.IPv4

final case class HttpServerConfig(
    host: String Refined IPv4,
    port: Port
) extends DetailedToString
