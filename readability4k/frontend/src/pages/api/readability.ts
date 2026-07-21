import type { APIRoute } from 'astro';
import { Readability } from '@mozilla/readability';
import { JSDOM } from 'jsdom';

export const POST: APIRoute = async ({ request }) => {
  let url: string;
  try {
    const body = await request.json();
    url = body.url;
  } catch {
    return new Response(JSON.stringify({ error: '잘못된 요청입니다.' }), {
      status: 400,
      headers: { 'Content-Type': 'application/json' },
    });
  }

  if (!url || typeof url !== 'string') {
    return new Response(JSON.stringify({ error: 'URL을 입력해주세요.' }), {
      status: 400,
      headers: { 'Content-Type': 'application/json' },
    });
  }

  let normalizedUrl: URL;
  try {
    normalizedUrl = new URL(url);
  } catch {
    return new Response(JSON.stringify({ error: '올바른 URL을 입력해주세요.' }), {
      status: 400,
      headers: { 'Content-Type': 'application/json' },
    });
  }

  let html: string;
  try {
    const response = await fetch(normalizedUrl.toString(), {
      headers: {
        'User-Agent':
          'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
      },
    });

    if (!response.ok) {
      return new Response(
        JSON.stringify({ error: `페이지를 불러올 수 없습니다. (HTTP ${response.status})` }),
        { status: 502, headers: { 'Content-Type': 'application/json' } }
      );
    }

    const contentType = response.headers.get('content-type') || '';
    if (!contentType.includes('text/html')) {
      return new Response(
        JSON.stringify({ error: 'HTML 페이지가 아닙니다.' }),
        { status: 400, headers: { 'Content-Type': 'application/json' } }
      );
    }

    html = await response.text();
  } catch (err) {
    const message = err instanceof Error ? err.message : '알 수 없는 오류';
    return new Response(
      JSON.stringify({ error: `페이지를 가져오는 중 오류가 발생했습니다: ${message}` }),
      { status: 502, headers: { 'Content-Type': 'application/json' } }
    );
  }

  try {
    const dom = new JSDOM(html, { url: normalizedUrl.toString() });
    const reader = new Readability(dom.window.document);
    const article = reader.parse();

    if (!article) {
      return new Response(
        JSON.stringify({ error: '본문을 추출할 수 없습니다.' }),
        { status: 422, headers: { 'Content-Type': 'application/json' } }
      );
    }

    return new Response(JSON.stringify(article), {
      headers: { 'Content-Type': 'application/json' },
    });
  } catch (err) {
    const message = err instanceof Error ? err.message : '알 수 없는 오류';
    return new Response(
      JSON.stringify({ error: `본문 추출 중 오류가 발생했습니다: ${message}` }),
      { status: 500, headers: { 'Content-Type': 'application/json' } }
    );
  }
};
