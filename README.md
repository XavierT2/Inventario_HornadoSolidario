# Hornado Solidario POS System 🥘🍺

Este es el sistema de Punto de Venta (POS) diseñado específicamente para funcionar sin internet en una red local. Está dividido en dos partes principales: el servidor (Backend Java) y la interfaz visual (Frontend Vue).

## 🚀 ¿Cómo iniciar el sistema el día del evento?

Para que el sistema funcione en todas las tablets y computadoras, debes encender ambos programas en la computadora principal que actuará como "Servidor".

### Paso 1: Iniciar el Servidor (Base de Datos y Lógica)
1. Abre una terminal (o consola de comandos).
2. Entra a la carpeta del backend: `cd backend`
3. Ejecuta el siguiente comando: 
   ```bash
   mvn quarkus:dev
   ```
4. *¡Listo! Déjalo encendido en esa ventana de fondo. Cuando veas que carga el porcentaje, la base de datos se ha levantado.*

### Paso 2: Iniciar la Interfaz Visual (Punto de Venta)
1. Abre **OTRA** terminal nueva.
2. Entra a la carpeta del frontend: `cd frontend`
3. Ejecuta el comando mágico:
   ```bash
   npm run dev
   ```
4. *Al iniciar, la consola te mostrará una IP local azul (ej. `http://192.168.0.22:5173`).*

### Paso 3: Usar el Sistema
- **En la computadora principal:** Entra desde el navegador (Chrome, Edge) a `http://localhost:5173`
- **Desde cualquier otro Celular o Tablet:** Conéctate al mismo Wi-Fi del computador principal y entra a la dirección azul que te dio el frontend (Ejem: `http://192.168.0.22:5173`).

---

## 🧹 ¿Cómo empezar las ventas desde cero? (Limpiar pruebas)

La base de datos completa de todas tus ventas e inventario es un pequeño archivito llamado **`hornado.db`** que vive dentro de la carpeta `backend`.
Para borrar todo y dejar la caja registradora lista para el evento inaugural:

1. Ve a la consola del Backend y detenla presionando `Ctrl + C`.
2. Entra a la carpeta `backend` desde tus explorador de archivos de Windows.
3. Busca el archivo `hornado.db` y **alimínalo**.
4. ¡Vuelve a ejecutar `mvn quarkus:dev`! 
   - El sistema detectará que todo está virgen, recreará una nueva base de datos limpia y añadirá únicamente los 3 productos base. ¡Tu caja registradora volverá a estar en **$0.00** de nuevo!

🔥 *Nota: Cualquier Excel descargado del Cierre de Caja guardará los totales justo hasta el momento de descarga.*
