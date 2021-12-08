<script>
    import {login, title, variables, gateway} from "$lib/variables";
    import {formSubmit} from "$lib/util";

    title.set("Register");

    let emailRegex =
        /(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])/;
    let passwordRegex = /(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}/;

    let error;
    let success = false;
    let registerForm;

    let good = "&check;", bad = "&cross;";
    let userStatus = bad,
        emailStatus = bad,
        passwordStatus = bad,
        confStatus = bad;
    let user, pass, passConf, email;
    let emailError, userError,
        userValid = false,
        passwordValid = false;

    $: {
        passwordValid = passwordRegex.test(pass);
        passwordStatus = passwordValid ? good : bad;
    }

    $: if (email) {
        if (!emailRegex.test(email)) {
            emailError = "Email is not valid.";
            emailStatus = bad;
        } else {
            emailError = undefined;
            emailStatus = good;
        }
    } else {
        emailError = undefined;
        emailStatus = bad;
    }

    $: if (user) {
        if (user.length > 3) {
            userError = undefined;
            userStatus = "loading";
            //Verify that the username is valid
            fetch(gateway() + "/auth-service/checkuser/" + user)
                .then(res => res.json())
                .then(data => {
                    if (data.error || !data.available) {
                        if (!data.available)
                            userError = "Username already taken.";
                        else
                            userError = `Server issues. Try again later. (${data.error})`;
                        userStatus = bad;
                        return;
                    }

                    userError = undefined;
                    userStatus = good;
                })
                .catch(error => {
                    userError = "Unknown error: " + error.message + " (Are the auth servers down?)";
                    userStatus = bad;
                });
        } else {
            userError = "Username too short!";
            userStatus = bad;
        }
    } else {
        userStatus = bad;
    }

    $: confStatus = pass && pass == passConf ? good : bad;

    const register = () => {
        let valid = true;
        // Validate our form data.
        if (!passwordValid || pass != passConf) valid = false;
        else if (emailError) valid = false;
        else if (userError) valid = false;

        if (!valid) {
            error = "Invalid form data.";
            return;
        }

        // Form data is good, we can submit :D
        formSubmit(registerForm, (data) => {
            if (data.error) {
                error = data.message.message;
                return;
            }
            if (data.token) {
                let token = data.token;
                login(token);
                success = true;
            }
        });
    };
</script>

<div class="full vcenter hcenter">
    {#if success}
        <div>
            <h1 class="success">&check; Successfully registered!</h1>
            <a href="/" class="button">Back Home</a>
        </div>
    {:else}
        <h1>Please fill out the form!</h1>
        <form bind:this={registerForm} id="registerForm" action="{gateway()}/auth-service/register"
              method="post">
            {#if error}
                <div class="error">{error}</div>
            {/if}
            <label for="username">Username</label>
            <div class="inputContainer">
                <input id="username" name="username" placeholder="Username" type="text"
                       bind:value={user}/>
                {#if userStatus == "loading"}
                    <img class="error buffer" src="/buffer.gif" alt="Checking Username"/>
                {:else}
                    <strong class="{userStatus == bad ? `error`: `good`}">{@html userStatus}</strong>
                {/if}
            </div>
            {#if userError}
                <div class="error"><strong>&cross;</strong> {userError}</div>
            {/if}

            <label for="email">Email</label>
            <div class="inputContainer">
                <input id="email" name="email" placeholder="Email" type="text"
                       bind:value={email}/>
                <strong class="{emailStatus == bad ? `error`: `good`}">{@html emailStatus}</strong>
            </div>
            {#if emailError}
                <div class="error"><strong>&cross;</strong> {emailError}</div>
            {/if}

            <label for="password">Password</label>
            <div class="inputContainer">
                <input id="password" name="password" placeholder="Password" type="password"
                       bind:value={pass}/>
                <strong class="{passwordValid ? `good`: `error`}">{@html passwordStatus}</strong>
            </div>
            {#if !passwordValid && pass}
                <div class="error"><strong>&cross;</strong> Password must contain at least one
                    letter, one number, and a special character.
                </div>
            {/if}

            <label for="confPassword">Confirm Password</label>
            <div class="inputContainer">
                <input id="confPassword" name="confPassword" placeholder="Confirm Password" type="password"
                       bind:value={passConf}/>
                <strong class="{confStatus == bad ? `error`: `good`}">{@html confStatus}</strong>
            </div>
            {#if pass && passConf && (pass != passConf)}
                <div class="error"><strong>&cross;</strong> Passwords do not match.</div>
            {/if}

            <div class="button" on:click={register}>Register</div>
        </form>
    {/if}
</div>

<style>
    form {
        display: flex;
        flex-direction: column;
        width: 50%;
        margin: 0 auto;
    }

    .inputContainer {
        display: flex;
        flex-direction: row;
    }

    .inputContainer input {
        flex-grow: 1;
        margin-left: 5px;
    }

    label {
        font-size: 1.2em;
        font-weight: bold;
        text-decoration: underline;
    }

    h1.success {
        color: green;
    }

    div.full {
        flex-direction: column;
    }

    .error {
        color: red;
        margin: 0 5px;
    }

    .good {
        color: green;
        margin: 0 5px;
    }

    .buffer {
        height: 1em;
    }
</style>