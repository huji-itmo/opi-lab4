
const API_ENDPOINT = "/controller"

const onSubmit = (event) => {
    if (!form_validator.checkAllValid()) {
        return;
    }

    changeBoardR(form_validator.form_r_input.value)
}

const onRInputChange = (event, ui) => {
    console.log("sadsd")
    changeBoardR(ui.value.toFixed(0));
}

class FormValidator {
    form_x_input;
    form_y_input;
    form_r_input;
    submit_button;

    checkXInput = () => {
        if (this.form_x_input.value === "") {
            alert("Выберите значение.");
            return false;
        }

        // alert("");
        return true;
    };

    checkYInput = () => {
        if (this.form_y_input.value === "") {
            alert(
                "Это поле не должно быть пустым."
            );
            return false;
        }

        let pattern = new RegExp(/^-?\d+([\.,]\d+)?$/, "g");

        if (!pattern.test(this.form_y_input.value)) {
            alert("Введите число.");
            return false;
        }

        if (this.form_y_input.value > 3 || this.form_y_input.value < -5) {
            alert(
                "Число y должно быть в пределе [-5, 3]."
            );
            return false;
        }

        // alert("");
        return true;
    };

    checkRInput = () => {
        if (this.form_r_input.value === "") {
            alert("Выберите значение.");
            return false;
        }

        if (!["1", "2", "3", "4", "5"].includes(this.form_r_input.value)) {
            this.form_r_input.value = "";
            alert("Выберите НОРМАЛЬНОЕ значение.");
            return false;
        }

        // alert("");
        return true;
    };

    constructor() {
        this.form_x_input = document.getElementById("main-form:form-x-input");
        this.form_y_input = document.getElementById("main-form:form-y-input");
        this.form_r_input = document.getElementById("main-form:form-r-input");
        this.submit_button = document.getElementById("main-form:submit-button");

        this.submit_button.addEventListener("click", onSubmit);
        this.form_r_input.addEventListener("change", onRInputChange);
    }

    checkAllValid = () => {
        let isNotValid = [
            !this.checkRInput(),
            !this.checkYInput(),
            !this.checkXInput(),
        ].reduce((prev, curr) => prev || curr);

        return !isNotValid;
    }
}

const form_validator = new FormValidator();
const table_element = $("#table");

const addPointFromData = (json) => {
    let tableEntry = "<tr>";
    let data = [json.x, json.y, json.r, json.hit, json.duration_milliseconds, json.server_time];
    data.forEach((str) => {
        tableEntry += `<td>${str}</td>`
    });
    tableEntry += "</tr>";

    table_element.html(table_element.html() + tableEntry);

    createNewHitPoint(
        json.x,
        json.y,
        json.r,
        {
            size: pointRadius,
            name: json.server_time,
            color: json.hit ? "green" : "red",
        }
    )
}


async function loadHitHistory() {

    changeBoardR((+form_validator.form_r_input.value).toFixed(0));

    let json = JSON.parse(generatedJsonString)

    let i = 0;
    while(json[i] != undefined) {
        createNewHitPoint(
            json[i].x,
            json[i].y,
            json[i].r,
            {
                size: pointRadius,
                name: json[i].server_time,
                color: json[i].hit ? "green" : "red",
            }
        )
        i++;
    }
}

loadHitHistory();

const afterAjax = (data) => {

    if (data.status !== 'success') {
        return;
    }

    x = form_validator.form_x_input.value;
    y = form_validator.form_y_input.value;
    r = form_validator.form_r_input.value;
    hit = document.getElementById("main-form:cached-hit").innerText == "true";
    server_time = document.getElementById("main-form:cached-server-time").innerText;

    console.log(x,y,r,hit,server_time)

    createNewHitPoint(
        x,
        y,
        r,
        {
            size: pointRadius,
            name: server_time,
            color: hit ? "green" : "red",
        }
    )
}

const onBoardClick = (x,y) => {
    form_validator.form_x_input.value = x.toFixed(0);
    form_validator.form_y_input.value = y.toFixed(2);
    // form_validator.form_r_input.value;

    form_validator.submit_button.dispatchEvent(new Event('click'));
}
