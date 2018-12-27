# Toggle Service

The purpose of this project is to allow admins to manage Toggle and
the services to query Toggle values. Toggle values are useful to control the 
behavior of features (also known as feature flag) and red/green testing.

A Toggle object consists of:
   - `name` - The Toggle's name. Example: `isButtonBlue`.
   - `defaultValue` -  The default value for all services.
   - `audience` - A list of service's names. When a Toggle have an audience, it value is not visible
   for services not target audience.
   - `blacklist` - A list of service's names that cannot see the toggle value.
   - `overriddenValues` - A list of service versions with its specific overridden values. A same service
   can have more different Toggle values for each version.


## Testing the application

Whenever a new dependency or file is changed, it is necessary to rebuild the test image:

```bash
docker-compose build test
```

The test image caches all the dependencies. It allows running the build command
before the tests without any overhead or additional network usage.

```bash
docker-compose build test && docker-compose run test
```

## Running the application

The application can be started using the `docker-compose up` command:

```bash
docker-compose build app && docker-compose up -d app
```

The `-d` argument starts the application in background mode, i.e. without attaching the current terminal. 
You can omit that argument to tail the application logs.

You can tail the application logs when the application is running in background mode executing the following command:

```bash
$ docker-compose logs -f app

Attaching to service_app_1
app_1   | Dec 22, 2018 10:17:33 PM io.vertx.core.Starter
app_1   | INFO: Succeeded in deploying verticle
app_1   | Dec 22, 2018 10:17:33 PM lab.pongoauth.MainVerticle
app_1   | INFO: MainVerticle started

```

## Environment Variables

The environment variables have default values to facilitate running locally. The environment
values used to run on Docker are defined in `docker-compose.yml`.

Those are the environment variables used by this service that can be set in production:

- `APP_TESTING_PORT` - Web Application Port used in tests. Default: `7070`.
- `WEBAPP_PORT` - Web Application Port. Default: `8080`.
- `RABBITMQ_USER` - RabbitMQ username. Default: `guest`.
- `RABBITMQ_PASS` - RabbitMQ password. Default: `guest`.
- `RABBITMQ_HOST` - RabbitMQ hostname. Default: `localhost`.
- `RABBITMQ_PORT` - RabbitMQ port. Default: `5672`.
- `ADMIN_PASS` - `admin` user password. Default: `admin`.
- `JWT_TOKEN_SECRET` - Passphrase used as symmetric key to issue JWT signed access tokens. 
Default: `WzqRd46wpCjJFGuunuGGfxqveo6zCCR1fw8MczQv`.
- `JWT_TOKEN_AUDIENCE` - "this.service.com";
- `JWT_TOKEN_ISSUER` - "this.service.com`";
- `JWT_TOKEN_LIFETIME_IN_SECONDS` - Access token time to live in seconds. Default: `300`;                           
- `MONGO_HOST` - Database host address. Default: '127.0.0.1'.
- `MONGO_PORT` - Database port. Default: '27017'.

## API Endpoints

The `token`, `ping`, and `toggle-values` endpoints do not require authenticated user.

The endpoints `toggle` and `toggles` require a user with `admin` role.

Before using the authenticated endpoints, the user must authenticate issuing a OAuth 2 access token. 

- `POST /oauth/token` - Issue a Access Token;
- `POST /toggles` - Creates a new Toggle;
- `GET /toggles` - List all toggles;
- `GET /toggle/:name` - Get a specific Toggle;
- `PUT /toggle/:name` - Update Toggle by name;
- `GET /services/:identifier/versions/:version/toggle-values` - Get the toggle values for a specific service;

A Postman Collection is available at: **`/doc/ToggleService.postman_collection.json`**

A Swagger definition for this API is available at: **`doc/swagger.yml`**.


## LADR - Lightweight Architecture Decision Records

Important decisions about frameworks, languages and how the project is organized should be 
documented in the /docs folder.

## Troubleshooting

### Mac Users

If you use a mac and JDK 8, the DSN resolution for localhost can present some delay. To fix this problem check your hostname and include localhost resolution
in the `/etc/hosts` file:

```bash
$ hostname
MacBook-Pro-de-Anderson.local

$ vim /etc/hosts
127.0.0.1       localhost
255.255.255.255 broadcasthost
::1             localhost
## Include this lines
127.0.0.1   MacBook-Pro-de-Anderson.local
::1         MacBook-Pro-de-Anderson.local
```
