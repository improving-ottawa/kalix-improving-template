import {BeginAuthenticationRequest} from "../../../generated/com/example/gateway/api/authentication_service_pb";
import {getAuthClient} from "./clients";
import {HttpBody} from "../../../generated/google/api/httpbody_pb";

export function sendBeginAuthenticationRequest(req: BeginAuthenticationRequest) {
    var deadline = new Date();
    deadline.setSeconds(deadline.getSeconds() + 30);

    return new Promise<{ response: HttpBody }>((resolve, reject) => {
        getAuthClient().then(client => {
            client.oidcAuthentication(req, {
                    //TODO: uncomment for routes that are authorized using JWT
                    //authorization: 'Bearer ' + window.BEARER_TOKEN,
                    deadline: deadline.getTime().toString()
                },
                (err, response) => {
                    if (err) {
                        console.error(`Unexpected error sending BeginAuthenticationRequest: code = ${err.code}` +
                            `, message = "${err.message}"`)
                        reject(err)
                    } else {
                        const resp = {response: response}
                        console.log(resp)
                        resolve(resp)
                    }
                })
        }).catch(err => {
            console.error(
                `Unexpected error creating client: code = ${err.code}` +
                `, message = "${err.message}"`
            )
            reject(err)
        })

    });

}
