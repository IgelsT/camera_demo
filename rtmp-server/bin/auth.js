const axios = require("axios");
const apiUrl = "http://camera.imile.ru/api/v1";

class AuthFromAPI {
    test() {
        console.log("FROM AuthFromAPI");
    }

    async authPublish(args) {
        if (args.authToken == undefined) throw "No token";
        // console.log(args);
        const data = { action: "publishAuthNMS" };
        try {
            const result = await this.RequestAPI(
                apiUrl + "/rtmpauth-nms",
                data,
                args.authToken,
            );
            return true;
        } catch (e) {
            throw e;
        }
    }

    async authPlay(args) {
        if (args.authToken == undefined) throw "No token";
        // console.log(args);
        const data = { action: "playAuthNMS" };
        try {
            const result = await this.RequestAPI(
                apiUrl + "/rtmpauth-nms",
                data,
                args.authToken,
            );
            return true;
        } catch (e) {
            throw e;
        }
    }

    async RequestAPI(url, data, token) {
        const error = {
            message: "",
            code: 0,
            reason: "",
        };
        const headers = { Authorization: token };
        try {
            const response = await axios({
                url: url,
                data: data,
                headers: headers,
                method: "POST",
                responseType: "json",
            });
            // console.log(response);
            if (response.headers["content-type"] != "application/json") {
                return response;
            } else if (response.data && response.data.result == "ok") {
                return response.data.data;
            } else if (response.data && response.data.result == "error") {
                error.message = response.data.data.error.message;
                error.code = response.data.data.error.code;
                error.reason = response.data.data.error.reason;
            } else {
                error.message = "Empty response";
                error.code = 2;
            }
        } catch (er) {
            if (er instanceof Error) {
                error.message = er.message;
                error.code = 1;
            }
            // showSnack(false, error.message);
            // throw error;
        }
        // console.log(error);
        throw error;
    }
}

module.exports = AuthFromAPI;
