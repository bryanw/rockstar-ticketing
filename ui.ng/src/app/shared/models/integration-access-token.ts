export class IntegrationAccessToken {
    constructor(public tokenType: string,
                public accessToken: string,
                public expiresIn: number) {

    }
}
