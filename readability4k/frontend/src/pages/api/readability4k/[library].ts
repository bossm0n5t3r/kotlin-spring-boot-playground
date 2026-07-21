import type { APIRoute } from 'astro';

export const GET: APIRoute = async ({ request, params }) => {
  const library = params.library;
  const url = new URL(request.url);
  const link = url.searchParams.get('link');

  if (!library) {
    return new Response(JSON.stringify({ error: '라이브러리 이름이 필요합니다.' }), {
      status: 400,
      headers: { 'Content-Type': 'application/json' },
    });
  }

  if (!link) {
    return new Response(JSON.stringify({ error: 'URL이 필요합니다.' }), {
      status: 400,
      headers: { 'Content-Type': 'application/json' },
    });
  }

  const backendHost = process.env.BACKEND_URL || 'http://localhost:1031';
  const backendUrl = `${backendHost}/readability4k/${library}?link=${encodeURIComponent(link)}`;

  try {
    const response = await fetch(backendUrl);
    const data = await response.json();
    return new Response(JSON.stringify(data), {
      status: response.status,
      headers: { 'Content-Type': 'application/json' },
    });
  } catch (err) {
    const message = err instanceof Error ? err.message : '알 수 없는 오류';
    return new Response(
      JSON.stringify({ error: `백엔드 요청 실패: ${message}` }),
      { status: 502, headers: { 'Content-Type': 'application/json' } }
    );
  }
};
