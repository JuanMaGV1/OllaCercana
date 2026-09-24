# OllaCercana

## Justificacion patrones

- Observer: Se aplica para desacoplar la publicación de eventos de dominio (cambios en reservas, disponibilidad de platos, chat) de las acciones que reaccionan a ellos (enviar push por FCM, guardar notificación in-app, difundir por WebSocket). El dominio publica el evento sin saber quién lo consume; la capa de aplicación define el contrato ObservadorReserva; la infraestructura implementa los notificadores concretos. Esto permite agregar nuevos canales de notificación sin modificar el dominio (OCP) y mantener bajo acoplamiento entre módulos (OC-015, OC-016).

- Factory Method: Se aplica para centralizar la creación de cuentas según su rol (FabricaComprador, FabricaCocinera, FabricaAdministrador). Encapsula las reglas de creación (validar unicidad de correo y celular, hashear contraseña, asignar rol inicial) en un único punto. Evita que los servicios construyan cuentas con new disperso y garantiza que toda cuenta creada cumpla las invariantes del dominio (OC-001).

- Chain of Responsibility: Se aplica para modelar el flujo de moderación de reportes y pausas por reputación. Cada eslabón (ValidadorReporte, EvaluadorReputacion, OcultamientoPreventivo, RevisorAdministrador) decide si puede manejar el caso o lo pasa al siguiente. Esto permite agregar reglas de moderación sin tocar las existentes, y separa la responsabilidad de cada paso: validar formato, evaluar reputación (RN-09), ocultar preventivamente (RN-22) y tomar la decisión final con justificación (RN-23) (OC-017, OC-018).

- Iterator / Composite: Se aplican conjuntamente para el catálogo de platos. Composite permite tratar un plato individual (PlatoHoja) o un grupo de platos (GrupoPlatos, agrupados por conjunto o cocinera) de forma uniforme, facilitando la organización jerárquica del catálogo. Iterator permite recorrer esa estructura aplicando filtros (tipo de comida, restricciones, precio, distancia) sin exponer la representación interna de la colección. Juntos resuelven la búsqueda y filtrado de platos de forma extensible y desacoplada (OC-006, OC-007, OC-008).

## Diagrama de clases

![diagramaClases](/olla-cercana/docs/uml/DiagramaClases.png)

## Diagrama C4
![diagramaC4](/olla-cercana/docs/uml/DiagramaC4.png)

## Diagrama de Componentes Generales
![diagramaComponentesGenerales](/olla-cercana/docs/uml/DiagramaComponentesGeneral.png)

## Diagrama de Componentes Especificos
![diagramaComponentesEspecifico](/olla-cercana/docs/uml/DiagramaComponentesEspecifico.png)

## Diagrama Casos de uso Administrador
![diagramaCasoAdministrador](/olla-cercana/docs/uml/DiagramaCasosUsoAdministrador.png)

## Diagrama Casos de uso Cocinero
![diagramaCasoCocinero](/olla-cercana/docs/uml/DiagramaCasosUsoCocinera.png)

## Diagrama Casos de uso Comprador
![diagramaCasoComprador](/olla-cercana/docs/uml/DiagramaCasosUsoComprador.png)