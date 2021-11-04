<script>
    import {gateway, title, variables} from "$lib/variables";
    import {fly, scale, slide} from "svelte/transition";
    import {elasticInOut, quintOut} from "svelte/easing";
    import {onDestroy, onMount} from "svelte";

    title.set("Shopping List");

    const custom = (node, {
        delay = 0,
        duration = 400,
        easing: easing$1 = elasticInOut,
        start = 0,
        opacity = 0,
        enabled = true
    } = {}) => {
        if (!enabled) return;

        const style = getComputedStyle(node);
        const target_opacity = +style.opacity;
        const transform = style.transform === 'none' ? '' : style.transform;
        const sd = 1 - start;
        const od = target_opacity * (1 - opacity);
        return {
            delay,
            duration,
            easing: easing$1,
            css: (_t, u) => `
			position: absolute;
			transform: ${transform} scale(${1 - (sd * u)});
			opacity: ${target_opacity - (od * u)}
		`
        };
    };


    let items = [
        {
            text: "",
            checked: false,
            uid: {}
        }
    ];

    let itemContainer, dataList = [];
    let complete = 0;

    $: {
        complete = 0;
        items.forEach(item => {
            if (item.checked)
                complete++;
        });

        if (items.length == 0 || (items.length > 0 && items[items.length - 1].text)) {
            items.push({text: "", checked: false, uid: {}});
        }

        if (items.length > 1 && !items[items.length - 2].text) {
            items.pop();
            items[items.length - 1].checked = false;
        }
    }

    const getList = () => {
        fetch(gateway() + "/auth-service/list")
            .then(res => res.json())
            .then(data => {
                console.log(data);
                let list = [];

                for (let e in data) {
                    list.push(data[e]);
                }

                items = list;
                items.forEach(itm => {
                    itm.uid = {};
                    itm.id = undefined;
                });
            })
            .catch(e => {
                console.error(e);
            });
    };

    const saveList = () => {
        let tmp = [...items];
        tmp.pop();

        let map = {};
        for (let i = 0; i < tmp.length; i++) {
            map[i] = tmp[i];
        }
        fetch(gateway() + "/auth-service/list", {
            method: "post",
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(map)
        })
            .then(res => res.json())
            .then(data => {
                if (!data?.success) {
                    alert("Shopping list could not be saved!");
                }
            })
            .catch(err => {
                console.error(err);
            });
    };

    let unsubscribe = variables.jwt.subscribe(getList);
    onMount(getList);
    onDestroy(unsubscribe);

    let itmCount = 0;
    $: itmCount = items.length - 1;

    const checkNext = (e, index) => {
        if (e.keyCode == 13) {
            let next = document.getElementById("input" + (index + 1));
            if (next) next.focus();
        }
    };

    const checkPrev = (e, index) => {
        if (e.keyCode == 8 && e.target.value == "") {
            let prev = document.getElementById("input" + (index - 1));
            if (prev) prev.focus();
        }
    };

    const check = index => {
        let itm = items[index];
        itm.checked = !itm.checked;
        items = [...items];
        saveList();
    };

    let pos1 = 0, pos2 = 0;
    let tracking, activeIndex, targetIndex = -1;
    const dragStart = e => {
        e.preventDefault();
        tracking = e.target;
        targetIndex = activeIndex = parseInt(tracking.getAttribute("index"));
        pos1 = e.clientY;
    };

    const dragging = e => {
        if (!tracking) return;
        e.preventDefault();
        pos2 = pos1 - e.clientY;
        pos1 = e.clientY;

        tracking.style.top = (tracking.offsetTop - pos2) + "px";
    };

    const endDrag = e => {
        if (!tracking) return;

        e.preventDefault();
        let itm = items[activeIndex];

        let tmp = [];
        if (targetIndex < 0 || targetIndex == NaN) targetIndex = activeIndex;
        console.log(targetIndex)
        for (let i = 0; i < items.length; i++) {
            if (targetIndex == i)
                tmp.push(itm);

            if (activeIndex == i) continue;

            tmp.push(items[i]);
        }

        tracking.style = undefined;
        items = [...tmp];
        tracking = undefined;
        activeIndex = targetIndex = -1;
        saveList();
    };

    const dragOver = e => {
        if (!tracking || e.target == null) return;
        let target = e.target;
        while (!target.getAttribute("index") && target.parentNode) {
            target = target.parentNode;
        }
        e.preventDefault();
        targetIndex = parseInt(target.getAttribute("index"));
    };

    const getApplicableItems = (target) => {
        let input = target.value;

        fetch(variables.gateway + "/item-service/items/search?query=" + input)
            .then(res => res.json())
            .then(data => {
                console.log(data);
                dataList = data;
            })
            .catch(e => {
                console.error(e);
            });
    };

</script>

<div class="container" out:slide>
    <div class="header">
        <div class="count">
            {#key itmCount}
                <span style="display: inline-block" in:fly={{y: -10}}>{itmCount}</span>
            {/key}
            items.
        </div>
        <div class="complete">
            (
            {#key complete}<span style="display: block" in:fly={{y: -10}}>{complete}</span>{/key}/
            {#key itmCount}<span style="display: block" in:fly={{y: -10}}>{itmCount}</span>{/key}
            &nbsp;complete)
        </div>
    </div>
    <div bind:this={itemContainer} class="items"
         on:mouseleave={endDrag}
         on:mousemove={dragging}
         on:mouseup={endDrag}>
        {#each items as itm, i (itm.uid)}
            {#if targetIndex == i}
                <div class="filler row"><span class="material-icons-round drag">keyboard_double_arrow_right</span>
                    <div class="material-icons-round check">
                        check_box_outline_blank
                    </div>
                    <span class="wrapper">
                        <span class="placeholder">{items[activeIndex].text}</span>
                        <input type="text" placeholder="{items[activeIndex].text}"/>
                    </span>
                </div>
            {/if}
            <div class="row" draggable="{itm.text ? true : false}" on:dragstart={dragStart}
                 class:active={activeIndex == i}
                 index="{i}"
                 on:mousemove={dragOver}
                 transition:slide>
                <span class="material-icons-round dr" class:drag={itm.text}>keyboard_double_arrow_right</span>
                {#if itm.text}
                    {#if itm.checked}
                        <div class="material-icons-round check"
                             on:click={() => check(i)}
                             out:custom={{duration: 500}}
                             in:scale={{duration: 750, easing: elasticInOut}}>
                            check_box
                        </div>
                    {:else}
                        <div class="material-icons-round check"
                             on:click={() => check(i)}
                             out:custom={{duration: 500}}
                             in:scale={{duration: 750, easing: elasticInOut}}>
                            check_box_outline_blank
                        </div>
                    {/if}
                {:else}
                    <div class="material-icons-round add"
                         in:scale={{duration: 750, easing: elasticInOut}}
                         out:custom={{enabled: itm.text, duration: 500, easing: quintOut}}>add
                    </div>
                {/if}

                <span class="wrapper">
                    <span class="placeholder">{itm.text}</span>
                    <input id="input{i}" type="text" bind:value="{itm.text}" placeholder="New item..."
                           list="data"
                           on:focus={e => getApplicableItems(e.target)}
                           on:keyup={e => getApplicableItems(e.target)}
                           on:keypress={e => checkNext(e, i)}
                           on:keydown={e => checkPrev(e, i)}/>
                </span>
            </div>
        {/each}
        <datalist id="data">
            {#each dataList as data}
                <option value="{data}"/>
            {/each}
        </datalist>
    </div>

    <div class="button" on:click={saveList}>Save List</div>
</div>

<style>
    .container {
        position: relative;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        /*width: 30%;*/
        min-width: 200px;
        margin: 0 auto;
        flex-grow: 1;
    }

    .header {
        width: 100%;
    }

    .row {
        position: relative;
        padding: 0.5em;
        background: var(--bg-color);
        border-bottom: 1px solid;
        border-image-source: linear-gradient(to right, rgba(0, 0, 0, 0), var(--disabled-fg-color),
        rgba(0, 0, 0, 0), rgba(0, 0, 0, 0));
        border-image-slice: 1;
        display: flex;
        align-items: center;
        transition: border 1s linear, transform 0.2s ease-in-out;
    }

    .row.active {
        position: absolute;
        pointer-events: none;
        z-index: 5;
        transform: rotate(15deg);
    }

    .items > div:last-child {
        /*border-bottom: 1px solid;*/
        border-bottom: none;
    }

    .add, .check {
        margin-right: 0.5em;
        user-select: none;
    }

    .check:hover {
        cursor: pointer;
    }

    input {
        /*min-width: 100%;*/
        flex-grow: 1;
        border: none;
        border-left: 1px solid black;
        padding: 0 0 0 0.5em;
        background: transparent;
        margin: 0;
        font-size: 1em;
        position: relative;
    }

    input::-webkit-calendar-picker-indicator {
        display: none !important;
    }

    input:focus {
        outline: none;
    }

    .complete, .header {
        display: flex;
    }

    .count {
        flex-grow: 1;
    }

    .complete {
        margin-left: 0.5em;
        color: var(--disabled-fg-color)
    }

    .dr {
        color: #ccc;
    }

    .drag:hover {
        cursor: move;
    }

    .filler {
        min-height: 1em;
        background: var(--secondary-color);
    }

    .placeholder {
        padding-left: 0.5em;
        padding-right: 2em;
        white-space: nowrap;
        font-size: 1em;
        height: 0px;
        overflow: hidden;
    }

    .wrapper {
        display: flex;
        flex-direction: column;
        flex: 1;
    }

    .button {
        width: 30%;
        min-width: 200px;
    }
</style>